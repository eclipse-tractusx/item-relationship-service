/********************************************************************************
 * Copyright (c) 2022,2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 * Copyright (c) 2021,2024 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

package org.eclipse.tractusx.irs.registryclient.decentral;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.irs.registryclient.decentral.exception.CreateDtrShellException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RequiredArgsConstructor
public class DigitalTwinRegistryService {

    private final RestTemplate restTemplate;
    private final String createShellUrl;

    public AssetAdministrationShellDescriptor createShell(
            final AssetAdministrationShellDescriptor assetAdministrationShellDescriptor)
            throws CreateDtrShellException {
        final ResponseEntity<AssetAdministrationShellDescriptor> createdShellResponse;
        try {
            createdShellResponse = restTemplate.postForEntity(createShellUrl,
                    new HttpEntity<>(assetAdministrationShellDescriptor, headers()),
                    AssetAdministrationShellDescriptor.class);
            final HttpStatusCode responseCode = createdShellResponse.getStatusCode();

            if (responseCode.value() == HttpStatus.CREATED.value()) {
                return createdShellResponse.getBody();
            }
        } catch (RestClientException e) {
            throw new CreateDtrShellException(e);
        }
        throw new CreateDtrShellException(
                "Failed to create shell %s".formatted(assetAdministrationShellDescriptor.getGlobalAssetId()));
    }

    private HttpHeaders headers() {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
