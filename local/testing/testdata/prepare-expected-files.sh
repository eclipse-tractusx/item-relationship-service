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

PROVIDER_BPN=$1

bpn_regex_pattern='(BPNL)([a-zA-Z0-9]{12})'
replacement=$PROVIDER_BPN
directory="irs-cucumber-tests/src/test/resources/expected-files"

# Iterate over all files in the directory
for file in "$directory"/*; do
  # Check if the file is a regular file
  if [ -f "$file" ]; then
    # Use sed to replace the pattern and overwrite the file
    sed -i -E "s/$bpn_regex_pattern/$replacement/g" "$file"
    echo "Processed: $file"
  fi
done