#!/bin/bash
#
# Copyright (c) 2022,2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
# Copyright (c) 2021,2024 Contributors to the Eclipse Foundation
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
SCRIPT_PATH="local/testing/testdata/transform-and-upload.py"

API_KEY=$1
PROVIDER_BPN=$2
ALLOWED_BPN=$2

SUBMODEL_URL=$3
SUBMODEL_UPLOAD_URL=$4

DTR_URL=$5
DTR_UPLOAD_URL=$6

EDC_CONTROLPLANE_URL=$7
EDC_UPLOAD_URL=$8

EDC_DATAPLANE_URL=$9


upload_data() {
    local filePath="$1"
    local policyId="$2"

    python "$SCRIPT_PATH" \
        -f "$filePath" \
        -s "$SUBMODEL_URL" \
        -su "$SUBMODEL_UPLOAD_URL" \
        -a "$DTR_URL" \
        -au "$DTR_UPLOAD_URL" \
        -edc "$EDC_CONTROLPLANE_URL" \
        -eu "$EDC_UPLOAD_URL" \
        --edcBPN "$PROVIDER_BPN" \
        -d "$EDC_DATAPLANE_URL" \
        -k "$API_KEY" \
        -p "$policyId" \
        --allowedBPNs "$ALLOWED_BPN" \
        --aas3
}

policyTraceability="traceability-core"
policyNotAccepted="id-3.0-not-accepted"

upload_data "local/testing/testdata/CX_Testdata_v.1.7.0_PartType.json" $policyTraceability
upload_data "local/testing/testdata/CX_Testdata_v1.7.0_PartInstance-reduced.json" $policyTraceability
upload_data "local/testing/testdata/CX_Testdata_v1.7.0_PartInstance-not-accepted-policy.json" $policyNotAccepted
upload_data "local/testing/testdata/CX_Testdata_529_compatibility_SingleLevelBomAsBuilt.json" $policyTraceability
upload_data "local/testing/testdata/CX_Testdata_529_compatibility_SingleLevelBomAsPlanned.json" $policyTraceability
upload_data "local/testing/testdata/CX_Testdata_529_compatibility_SingleLevelUsageAsBuilt.json" $policyTraceability
upload_data "local/testing/testdata/CX_Testdata_529_compatibility_SingleLevelUsageAsPlanned.json" $policyTraceability
