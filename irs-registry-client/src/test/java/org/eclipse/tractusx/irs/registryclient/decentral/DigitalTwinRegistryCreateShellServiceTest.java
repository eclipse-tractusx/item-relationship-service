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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.irs.component.assetadministrationshell.Endpoint;
import org.eclipse.tractusx.irs.component.assetadministrationshell.IdentifierKeyValuePair;
import org.eclipse.tractusx.irs.component.assetadministrationshell.ProtocolInformation;
import org.eclipse.tractusx.irs.component.assetadministrationshell.Reference;
import org.eclipse.tractusx.irs.component.assetadministrationshell.SecurityAttribute;
import org.eclipse.tractusx.irs.component.assetadministrationshell.SemanticId;
import org.eclipse.tractusx.irs.component.assetadministrationshell.SubmodelDescriptor;
import org.eclipse.tractusx.irs.registryclient.decentral.exception.CreateDtrShellException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class DigitalTwinRegistryCreateShellServiceTest {

    private static final String CREATE_SHELL_URL = "/shell-descriptors";
    @Mock
    private RestTemplate restTemplate;
    private DigitalTwinRegistryCreateShellService service;

    @BeforeEach
    void setUp() {
        service = new DigitalTwinRegistryCreateShellService(restTemplate, CREATE_SHELL_URL);
    }

    @Test
    void givenShell_whenCreateShell_thenCreteIt() throws CreateDtrShellException {
        // given
        AssetAdministrationShellDescriptor shell = testShell();
        when(restTemplate.postForEntity(any(String.class), any(HttpEntity.class), any())).thenReturn(
                ResponseEntity.status(HttpStatus.CREATED).body(shell));

        // when
        AssetAdministrationShellDescriptor result = service.createShell(shell);

        // then
        assertThat(result).isNotNull();
    }

    @Test
    void givenShell_whenTemplateException_thenThrowException() {
        // given
        AssetAdministrationShellDescriptor shell = testShell();
        doThrow(new RestClientException("Surprise")).when(restTemplate)
                                                    .postForEntity(any(String.class), any(HttpEntity.class), any());

        // when/then
        assertThrows(CreateDtrShellException.class, () -> service.createShell(shell));
    }

    @Test
    void givenShell_whenConflict_thenThrowException() {
        // given
        AssetAdministrationShellDescriptor shell = testShell();
        when(restTemplate.postForEntity(any(String.class), any(HttpEntity.class), any())).thenReturn(
                ResponseEntity.status(HttpStatus.CONFLICT).body(shell));

        // when/then
        assertThrows(CreateDtrShellException.class, () -> service.createShell(shell));
    }

    @Test
    void testCreateShell() throws CreateDtrShellException {
        AssetAdministrationShellDescriptor shell = testShell();
        when(restTemplate.postForEntity(any(String.class), any(HttpEntity.class), any())).thenReturn(
                ResponseEntity.status(HttpStatus.CREATED).body(shell));

        // when
        AssetAdministrationShellDescriptor result = service.createShell(shell);

        // then
        assertThat(result).isNotNull();
    }

    private AssetAdministrationShellDescriptor testShell() {
        return AssetAdministrationShellDescriptor.builder()
                                                 .globalAssetId("urn:uuid:254604ab-2153-45fb-8cad-54ef09f4080f")
                                                 .idShort("test")
                                                 .id("urn:uuid:25300562-aa66-4840-8952-5cef4ed667c2")
                                                 .specificAssetIds(List.of(IdentifierKeyValuePair.builder()
                                                                                                 .name("manufacturerId")
                                                                                                 .value("BPNL00000003CNKC")
                                                                                                 .build()))
                                                 .submodelDescriptors(List.of(SubmodelDescriptor.builder()
                                                                                                .idShort(
                                                                                                        "SingleLevelUsageAsBuilt")
                                                                                                .id("urn:uuid:e401ccb7-a8f5-499a-9340-93746822d775")
                                                                                                .semanticId(
                                                                                                        Reference.builder()
                                                                                                                 .type("ExternalReference")
                                                                                                                 .keys(List.of(
                                                                                                                         SemanticId.builder()
                                                                                                                                   .type("GlobalReference")
                                                                                                                                   .value("urn:bamm:io.catenax.single_level_usage_as_built:2.0.0#SingleLevelUsageAsBuilt")
                                                                                                                                   .build()))
                                                                                                                 .build()

                                                                                                )
                                                                                                .endpoints(
                                                                                                        List.of(Endpoint.builder()
                                                                                                                        .interfaceInformation(
                                                                                                                                "SUBMODEL-3.0")
                                                                                                                        .protocolInformation(
                                                                                                                                ProtocolInformation.builder()
                                                                                                                                                   .href("https://trace-x-edc-dataplane.dev.demo.catena-x.net/api/public/data/urn:uuid:e401ccb7-a8f5-499a-9340-93746822d775")
                                                                                                                                                   .endpointProtocol(
                                                                                                                                                           "HTTP")
                                                                                                                                                   .endpointProtocolVersion(
                                                                                                                                                           List.of("1.1"))
                                                                                                                                                   .subprotocol(
                                                                                                                                                           "DSP")
                                                                                                                                                   .subprotocolBody(
                                                                                                                                                           "id=urn:uuid:cb6d86b5-b8ea-4fc0-b10b-ff2dd62f793d;dspEndpoint=https://trace-x-edc.dev.demo.catena-x.net")
                                                                                                                                                   .subprotocolBodyEncoding(
                                                                                                                                                           "plain")
                                                                                                                                                   .securityAttributes(
                                                                                                                                                           List.of(SecurityAttribute.none()))

                                                                                                                                                   .build())
                                                                                                                        .build()))
                                                                                                .build()))
                                                 .build();
    }
}