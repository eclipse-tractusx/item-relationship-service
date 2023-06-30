/********************************************************************************
 * Copyright (c) 2021,2022,2023
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2023: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2022,2023 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0. *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.irs.registryclient.central;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.List;

import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.irs.component.assetadministrationshell.IdentifierKeyValuePair;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

class DigitalTwinRegistryClientImplTest {

    private final RestTemplate restTemplate = mock(RestTemplate.class);

    private final DigitalTwinRegistryClientImpl digitalTwinRegistryClient = new DigitalTwinRegistryClientImpl(
            restTemplate, "url/{aasIdentifier}", "url/{assetIds}");

    @Test
    void shouldCallExternalServiceOnceAndGetAssetAdministrationShellDescriptor() {
        final String aasIdentifier = "urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6";
        doReturn(new AssetAdministrationShellDescriptor()).when(restTemplate)
                                                          .getForObject(any(),
                                                                  eq(AssetAdministrationShellDescriptor.class));

        final AssetAdministrationShellDescriptor assetAdministrationShellDescriptor = digitalTwinRegistryClient.getAssetAdministrationShellDescriptor(
                aasIdentifier);

        assertNotNull(assetAdministrationShellDescriptor);
        verify(this.restTemplate, times(1)).getForObject(any(), eq(AssetAdministrationShellDescriptor.class));
    }

    @Test
    void shouldCallExternalServiceOnceAndGetAssetAdministrationShellIds() {
        doReturn(ResponseEntity.ok(List.of("testId"))).when(restTemplate)
                                                      .exchange(any(), any(), any(),
                                                              any(ParameterizedTypeReference.class));

        final List<String> empty = digitalTwinRegistryClient.getAllAssetAdministrationShellIdsByAssetLink(
                Collections.emptyList());

        assertNotNull(empty);
        verify(this.restTemplate, times(1)).exchange(any(), any(), any(), any(ParameterizedTypeReference.class));
    }

    @Test
    void shouldCallExternalServiceOnceAndRunIntoTimeout() {
        final SocketTimeoutException timeoutException = new SocketTimeoutException("UnitTestTimeout");
        Throwable ex = new ResourceAccessException("UnitTest", timeoutException);
        doThrow(ex).when(restTemplate).exchange(any(), any(), any(), any(ParameterizedTypeReference.class));

        final List<IdentifierKeyValuePair> emptyList = Collections.emptyList();
        assertThrows(ResourceAccessException.class,
                () -> digitalTwinRegistryClient.getAllAssetAdministrationShellIdsByAssetLink(emptyList));

        verify(this.restTemplate, times(1)).exchange(any(), any(), any(), any(ParameterizedTypeReference.class));
    }
}
