#!/usr/bin/env bash
#
# Copyright (c) 2026 Volkswagen AG
#
# See the NOTICE file(s) distributed with this work for additional
# information regarding copyright ownership.
#
# This program and the accompanying materials are made available under the
# terms of the Apache License, Version 2.0 which is available at
# https://www.apache.org/licenses/LICENSE-2.0.
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations
# under the License.
#
# SPDX-License-Identifier: Apache-2.0
#

set -euo pipefail

mode="${1:?Usage: check-seaweedfs.sh prepare|verify}"
case "$mode" in
  prepare|verify) ;;
  *) echo "Unknown storage check: $mode" >&2; exit 1 ;;
esac

credentials=$(kubectl get secret seaweedfs-s3 -n storage-test -o json |
  jq -r '.data["s3.json"] | @base64d | fromjson | .identities[0].credentials[0] | "\(.accessKey):\(.secretKey)"')
endpoint=http://127.0.0.1:18333
bucket=irs-ci-storage-check
object=upgrade-check.txt
content='IRS Helm storage check'
work_dir=$(mktemp -d)
kubectl port-forward -n storage-test service/seaweedfs 18333:8333 > "$work_dir/port-forward.log" 2>&1 &
forward_pid=$!
trap 'kill "$forward_pid" 2>/dev/null || true; wait "$forward_pid" 2>/dev/null || true; rm -rf "$work_dir"' EXIT

s3_request() {
  curl --silent --show-error --fail-with-body --connect-timeout 5 --max-time 30 \
    --aws-sigv4 'aws:amz:us-east-1:s3' --user "$credentials" "$@"
}

s3_request --retry 20 --retry-connrefused --retry-delay 1 "$endpoint/" > /dev/null

if [[ "$mode" == prepare ]]; then
  # Check both unsigned access and an invalid signature against the configured identity.
  for auth in anonymous invalid; do
    curl_args=(--silent --show-error --connect-timeout 5 --max-time 30)
    if [[ "$auth" == invalid ]]; then
      curl_args+=(--aws-sigv4 'aws:amz:us-east-1:s3' --user "${credentials%%:*}:wrong-secret")
    fi
    status=$(curl "${curl_args[@]}" --output /dev/null --write-out '%{http_code}' "$endpoint/")
    [[ "$status" == 403 ]] || { echo "Expected HTTP 403 for $auth S3 access, got $status" >&2; exit 1; }
  done
  s3_request --request PUT "$endpoint/$bucket" > /dev/null
  lifecycle='<LifecycleConfiguration><Rule><ID>ci-expiration</ID><Filter><Prefix></Prefix></Filter><Status>Enabled</Status><Expiration><Days>7</Days></Expiration></Rule></LifecycleConfiguration>'
  checksum=$(printf '%s' "$lifecycle" | openssl dgst -md5 -binary | openssl base64)
  s3_request --request PUT --header 'Content-Type: application/xml' --header "Content-MD5: $checksum" \
    --data-binary "$lifecycle" "$endpoint/$bucket?lifecycle" > /dev/null
  s3_request --request PUT --data-binary "$content" "$endpoint/$bucket/$object" > /dev/null
fi

[[ "$(s3_request "$endpoint/$bucket/$object")" == "$content" ]]
s3_request "$endpoint/$bucket?list-type=2" | python3 -c \
  'import sys, xml.etree.ElementTree as ET; assert "upgrade-check.txt" in [node.text for node in ET.parse(sys.stdin).findall(".//{*}Contents/{*}Key")]'
s3_request "$endpoint/$bucket?lifecycle" | python3 -c \
  'import sys, xml.etree.ElementTree as ET; assert ET.parse(sys.stdin).find(".//{*}Expiration/{*}Days").text == "7"'

if [[ "$mode" == verify ]]; then
  s3_request --request DELETE "$endpoint/$bucket/$object" > /dev/null
  status=$(curl --silent --show-error --connect-timeout 5 --max-time 30 \
    --aws-sigv4 'aws:amz:us-east-1:s3' --user "$credentials" \
    --output /dev/null --write-out '%{http_code}' "$endpoint/$bucket/$object")
  [[ "$status" == 404 ]] || { echo "Expected HTTP 404 after object deletion, got $status" >&2; exit 1; }
  s3_request --request DELETE "$endpoint/$bucket" > /dev/null
fi

echo "S3 storage check ($mode) passed."
