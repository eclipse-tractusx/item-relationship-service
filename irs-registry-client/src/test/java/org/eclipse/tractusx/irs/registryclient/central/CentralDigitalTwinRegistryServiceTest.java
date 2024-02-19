/********************************************************************************
 * Copyright (c) 2022,2024
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2024: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
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
package org.eclipse.tractusx.irs.registryclient.central;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.tractusx.irs.component.Shell;
import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.irs.component.assetadministrationshell.Endpoint;
import org.eclipse.tractusx.irs.component.assetadministrationshell.SubmodelDescriptor;
import org.eclipse.tractusx.irs.data.CxTestDataContainer;
import org.eclipse.tractusx.irs.registryclient.DigitalTwinRegistryKey;
import org.eclipse.tractusx.irs.registryclient.DigitalTwinRegistryService;
import org.eclipse.tractusx.irs.registryclient.decentral.LookupShellsResponse;
import org.eclipse.tractusx.irs.registryclient.exceptions.RegistryServiceException;
import org.eclipse.tractusx.irs.testing.containers.LocalTestDataConfigurationAware;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;

@ExtendWith(MockitoExtension.class)
class CentralDigitalTwinRegistryServiceTest extends LocalTestDataConfigurationAware {

    private final String singleLevelBomAsBuiltURN = "urn:bamm:io.catenax.single_level_bom_as_built:1.0.0";
    private final String serialPartURN = "urn:bamm:io.catenax.serial_part:1.0.0";
    private DigitalTwinRegistryService digitalTwinRegistryService;
    @Mock
    private DigitalTwinRegistryClient dtRegistryClientMock;
    private CentralDigitalTwinRegistryService dtRegistryFacadeWithMock;

    CentralDigitalTwinRegistryServiceTest() throws IOException {
        super();
    }

    @BeforeEach
    void setUp() throws IOException {
        digitalTwinRegistryService = new CentralDigitalTwinRegistryService(
                new DigitalTwinRegistryClientLocalStub(localTestDataConfiguration.cxTestDataContainer()));
        dtRegistryFacadeWithMock = new CentralDigitalTwinRegistryService(dtRegistryClientMock);
    }

    @Test
    void shouldReturnSubmodelEndpointsWhenRequestingWithCatenaXId() throws RegistryServiceException {
        final String existingCatenaXId = "urn:uuid:a65c35a8-8d31-4a86-899b-57912de33675";

        final Collection<Shell> aasShellDescriptor = digitalTwinRegistryService.fetchShells(
                List.of(new DigitalTwinRegistryKey(existingCatenaXId, "")));
        final List<SubmodelDescriptor> shellEndpoints = aasShellDescriptor.stream()
                                                                          .findFirst()
                                                                          .get()
                                                                          .payload()
                                                                          .getSubmodelDescriptors();

        assertThat(shellEndpoints).isNotNull().isNotEmpty();
        final Endpoint endpoint = shellEndpoints.get(0).getEndpoints().get(0);

        assertThat(endpoint.getProtocolInformation().getSubprotocolBody()).contains(existingCatenaXId);
        assertThat(shellEndpoints.get(0).getSemanticId().getKeys().get(0).getValue()).isEqualTo(
                singleLevelBomAsBuiltURN);
        assertThat(shellEndpoints.get(1).getSemanticId().getKeys().get(0).getValue()).isEqualTo(serialPartURN);
    }

    @Test
    void shouldThrowExceptionWhenRequestError() {
        final String catenaXId = "test";
        final DigitalTwinRegistryKey key = new DigitalTwinRegistryKey(catenaXId, "");
        when(dtRegistryClientMock.getAssetAdministrationShellDescriptor(catenaXId)).thenThrow(
                new RestClientException("Dummy"));
        when(dtRegistryClientMock.getAllAssetAdministrationShellIdsByAssetLink(any())).thenReturn(
                LookupShellsResponse.builder().result(Collections.emptyList()).build());

        final var keys = List.of(key);
        assertThatExceptionOfType(RestClientException.class).isThrownBy(
                () -> dtRegistryFacadeWithMock.fetchShells(keys));
    }

    @Test
    void shouldReturnTombstoneWhenClientReturnsEmptyDescriptor() {
        final String catenaXId = "test";
        final AssetAdministrationShellDescriptor shellDescriptor = new AssetAdministrationShellDescriptor();
        shellDescriptor.setSubmodelDescriptors(List.of());

        when(dtRegistryClientMock.getAssetAdministrationShellDescriptor(catenaXId)).thenReturn(shellDescriptor);
        when(dtRegistryClientMock.getAllAssetAdministrationShellIdsByAssetLink(any())).thenReturn(
                LookupShellsResponse.builder().result(Collections.emptyList()).build());

        final List<SubmodelDescriptor> submodelEndpoints = dtRegistryFacadeWithMock.fetchShells(
                List.of(new DigitalTwinRegistryKey(catenaXId, ""))).stream().findFirst().get().payload().getSubmodelDescriptors();
        assertThat(submodelEndpoints).isEmpty();
    }

    @Test
    void verifyExecutionOfRegistryClientMethods() {
        final String catenaXId = "test";
        final AssetAdministrationShellDescriptor shellDescriptor = new AssetAdministrationShellDescriptor();
        shellDescriptor.setSubmodelDescriptors(List.of());

        when(dtRegistryClientMock.getAssetAdministrationShellDescriptor(catenaXId)).thenReturn(shellDescriptor);
        when(dtRegistryClientMock.getAllAssetAdministrationShellIdsByAssetLink(any())).thenReturn(
                LookupShellsResponse.builder().result(Collections.emptyList()).build());

        dtRegistryFacadeWithMock.fetchShells(List.of(new DigitalTwinRegistryKey(catenaXId, "")));

        verify(this.dtRegistryClientMock, times(1)).getAllAssetAdministrationShellIdsByAssetLink(anyList());
        verify(this.dtRegistryClientMock, times(1)).getAssetAdministrationShellDescriptor(catenaXId);
    }

    @Test
    void shouldReturnAssetAdministrationShellDescriptorForFoundIdentification() {
        final String identification = "identification";
        final String globalAssetId = "globalAssetId";
        final AssetAdministrationShellDescriptor shellDescriptor = new AssetAdministrationShellDescriptor();
        shellDescriptor.setSubmodelDescriptors(List.of());

        when(dtRegistryClientMock.getAllAssetAdministrationShellIdsByAssetLink(anyList())).thenReturn(
                LookupShellsResponse.builder().result(Collections.singletonList(identification)).build());
        when(dtRegistryClientMock.getAssetAdministrationShellDescriptor(identification)).thenReturn(shellDescriptor);

        final List<SubmodelDescriptor> submodelEndpoints = dtRegistryFacadeWithMock.fetchShells(
                                                                                           List.of(new DigitalTwinRegistryKey(globalAssetId, "")))
                                                                                   .stream()
                                                                                   .findFirst()
                                                                                   .get()
                                                                                   .payload()
                                                                                   .getSubmodelDescriptors();
        assertThat(submodelEndpoints).isEmpty();
    }

    @Test
    void shouldThrowErrorWhenCallingTestId() {
        final String globalAssetId = "urn:uuid:9ea14fbe-0401-4ad0-93b6-dad46b5b6e3d";
        final DigitalTwinRegistryClientLocalStub client = new DigitalTwinRegistryClientLocalStub(
                mock(CxTestDataContainer.class));

        assertThatExceptionOfType(RestClientException.class).isThrownBy(
                () -> client.getAssetAdministrationShellDescriptor(globalAssetId));
    }

    @Test
    void shouldReturnSubmodelEndpointsWhenFilteringByAspectType() throws RegistryServiceException {
        final String existingCatenaXId = "urn:uuid:a65c35a8-8d31-4a86-899b-57912de33675";

        final List<SubmodelDescriptor> shellEndpoints = digitalTwinRegistryService.fetchShells(
                                                                                          List.of(new DigitalTwinRegistryKey(existingCatenaXId, "")))
                                                                                  .stream()
                                                                                  .findFirst()
                                                                                  .get()
                                                                                  .payload()
                                                                                  .getSubmodelDescriptors();

        assertThat(shellEndpoints).isNotNull().isNotEmpty();
        final SubmodelDescriptor endpoint = shellEndpoints.get(0);

        assertThat(endpoint.getSemanticId().getKeys().get(0).getValue()).isEqualTo(singleLevelBomAsBuiltURN);
    }
}
