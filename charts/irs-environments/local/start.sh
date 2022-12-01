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
echo "Installing IRS-local Helm Charts"
helm install irs-local charts/irs-environments/local/ -n product-traceability-irs --create-namespace

echo "Waiting for the deployments to be available"
kubectl wait deployment -n product-traceability-irs --for condition=Available --timeout=90s irs-local-submodelservers
kubectl wait deployment -n product-traceability-irs --for condition=Available --timeout=90s irs-local-minio
kubectl wait deployment -n product-traceability-irs --for condition=Available --timeout=90s irs-local-edc-controlplane
kubectl wait deployment -n product-traceability-irs --for condition=Available --timeout=90s irs-local-edc-controlplane-provider
kubectl wait deployment -n product-traceability-irs --for condition=Available --timeout=90s irs-local-edc-dataplane
kubectl wait deployment -n product-traceability-irs --for condition=Available --timeout=90s irs-local-edc-dataplane-provider
kubectl wait deployment -n product-traceability-irs --for condition=Available --timeout=90s irs-local-irs-helm
kubectl wait deployment -n product-traceability-irs --for condition=Available --timeout=90s cx-irs-local-registry
