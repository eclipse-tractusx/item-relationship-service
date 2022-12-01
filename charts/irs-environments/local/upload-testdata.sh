#!/usr/bin/sh
#################################################################################
# Copyright (c) 2021,2022
#       2022: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
#       2022: ZF Friedrichshafen AG
#       2022: ISTOS GmbH
# Copyright (c) 2021,2022 Contributors to the Eclipse Foundation
#
# See the NOTICE file(s) distributed with this work for additional
# information regarding copyright ownership.
#
# This program and the accompanying materials are made available under the
# terms of the Apache License, Version 2.0 which is available at
# https://www.apache.org/licenses/LICENSE-2.0. *
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations
# under the License.
#
# SPDX-License-Identifier: Apache-2.0
##################################################################################
echo "Uploading testdata"
py.exe testdata-transform/transform-and-upload.py \
  -f testdata-transform/CX_Testdata_1.3.3-reduced-with-asPlanned.json \
  -s http://irs-local-submodelservers:8080 \
  -su http://localhost:10199 \
  -a http://localhost:10196 \
  -edc http://irs-local-edc-controlplane-provider \
  -eu http://localhost:10197 \
  -k 123456
