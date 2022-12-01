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
echo "Forwarding the deployed services to local machine"
kubectl port-forward deployment/irs-local-irs-helm 10165:8080 -n product-traceability-irs &
kubectl port-forward deployment/cx-irs-local-registry 10196:4243 -n product-traceability-irs &
kubectl port-forward deployment/irs-local-edc-controlplane-provider 10197:8181 -n product-traceability-irs &
kubectl port-forward deployment/irs-local-edc-controlplane 10198:8181 -n product-traceability-irs &
kubectl port-forward deployment/irs-local-submodelservers 10199:8080 -n product-traceability-irs