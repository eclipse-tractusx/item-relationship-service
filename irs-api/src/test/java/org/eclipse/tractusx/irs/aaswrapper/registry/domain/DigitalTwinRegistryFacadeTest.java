/********************************************************************************
 * Copyright (c) 2021,2022
 *       2022: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 * Copyright (c) 2021,2022 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.irs.aaswrapper.registry.domain;

<<<<<<< HEAD:irs-api/src/test/java/net/catenax/irs/aaswrapper/registry/domain/DigitalTwinRegistryFacadeTest.java
import static net.catenax.irs.util.TestMother.jobParameter;
=======
import static org.eclipse.tractusx.irs.util.TestMother.jobParameter;
import static org.eclipse.tractusx.irs.util.TestMother.jobParameterEmptyFilter;
import static org.eclipse.tractusx.irs.util.TestMother.jobParameterFilter;
>>>>>>> main:irs-api/src/test/java/org/eclipse/tractusx/irs/aaswrapper/registry/domain/DigitalTwinRegistryFacadeTest.java
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.irs.component.assetadministrationshell.Endpoint;
import org.eclipse.tractusx.irs.component.assetadministrationshell.SubmodelDescriptor;
import org.eclipse.tractusx.irs.dto.JobParameter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;

@ExtendWith(MockitoExtension.class)
class DigitalTwinRegistryFacadeTest {

    private final String assemblyPartRelationshipURN = "urn:bamm:com.catenax.assembly_part_relationship:1.0.0";
    private final String serialPartTypizationURN = "urn:bamm:com.catenax.serial_part_typization:1.0.0";
    private DigitalTwinRegistryFacade digitalTwinRegistryFacade;
    @Mock
    private DigitalTwinRegistryClient dtRegistryClientMock;
    private DigitalTwinRegistryFacade dtRegistryFacadeWithMock;

    @BeforeEach
    void setUp() {
        digitalTwinRegistryFacade = new DigitalTwinRegistryFacade(new DigitalTwinRegistryClientLocalStub());
        dtRegistryFacadeWithMock = new DigitalTwinRegistryFacade(dtRegistryClientMock);
    }

    @Test
    void shouldReturnSubmodelEndpointsWhenRequestingWithCatenaXId() {
        final String catenaXId = "urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6";

        final AssetAdministrationShellDescriptor aasShellDescriptor = digitalTwinRegistryFacade.getAAShellDescriptor(
                catenaXId);
        final List<SubmodelDescriptor> shellEndpoints = aasShellDescriptor.getSubmodelDescriptors();

        assertThat(shellEndpoints).isNotNull().hasSize(2);
        final Endpoint endpoint = shellEndpoints.get(0).getEndpoints().get(0);

        assertThat(endpoint.getProtocolInformation().getEndpointAddress()).isEqualTo(catenaXId);
        assertThat(shellEndpoints.get(0).getSemanticId().getValue()).containsExactly(assemblyPartRelationshipURN);
        assertThat(shellEndpoints.get(1).getSemanticId().getValue()).containsExactly(serialPartTypizationURN);
    }

    @Test
    void shouldThrowExceptionWhenRequestError() {
        final String catenaXId = "test";
        final JobParameter jobParameter = jobParameter();
        when(dtRegistryClientMock.getAssetAdministrationShellDescriptor(catenaXId)).thenThrow(
                new RestClientException("Dummy"));

        assertThatExceptionOfType(RestClientException.class).isThrownBy(
                () -> dtRegistryFacadeWithMock.getAAShellDescriptor(catenaXId));
    }

    @Test
    void shouldReturnTombstoneWhenClientReturnsEmptyDescriptor() {
        final String catenaXId = "test";
        final AssetAdministrationShellDescriptor shellDescriptor = new AssetAdministrationShellDescriptor();
        shellDescriptor.setSubmodelDescriptors(List.of());

        when(dtRegistryClientMock.getAssetAdministrationShellDescriptor(catenaXId)).thenReturn(shellDescriptor);

        final List<SubmodelDescriptor> submodelEndpoints = dtRegistryFacadeWithMock.getAAShellDescriptor(catenaXId).getSubmodelDescriptors();
        assertThat(submodelEndpoints).isEmpty();
    }

    @Test
    void verifyExecutionOfRegistryClientMethods() {
        final String catenaXId = "test";
        final AssetAdministrationShellDescriptor shellDescriptor = new AssetAdministrationShellDescriptor();
        shellDescriptor.setSubmodelDescriptors(List.of());

        when(dtRegistryClientMock.getAssetAdministrationShellDescriptor(catenaXId)).thenReturn(shellDescriptor);

        dtRegistryFacadeWithMock.getAAShellDescriptor(catenaXId);

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
                Collections.singletonList(identification));
        when(dtRegistryClientMock.getAssetAdministrationShellDescriptor(identification)).thenReturn(shellDescriptor);

        final List<SubmodelDescriptor> submodelEndpoints = dtRegistryFacadeWithMock.getAAShellDescriptor(globalAssetId).getSubmodelDescriptors();
        assertThat(submodelEndpoints).isEmpty();
    }

    @Test
    void shouldThrowErrorWhenCallingTestId() {
        final String globalAssetId = "urn:uuid:9ea14fbe-0401-4ad0-93b6-dad46b5b6e3d";
        final DigitalTwinRegistryClientLocalStub client = new DigitalTwinRegistryClientLocalStub();

        assertThatExceptionOfType(RestClientException.class).isThrownBy(
                () -> client.getAssetAdministrationShellDescriptor(globalAssetId));
    }

    @Test
    void shouldReturnSubmodelEndpointsWhenFilteringByAspectType() {
        final String catenaXId = "urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6";

        final List<SubmodelDescriptor> shellEndpoints = digitalTwinRegistryFacade.getAAShellDescriptor(catenaXId).getSubmodelDescriptors();

        assertThat(shellEndpoints).isNotNull().hasSize(2);
        final SubmodelDescriptor endpoint = shellEndpoints.get(0);

        assertThat(endpoint.getSemanticId().getValue()).containsExactly(assemblyPartRelationshipURN);
    }
}
