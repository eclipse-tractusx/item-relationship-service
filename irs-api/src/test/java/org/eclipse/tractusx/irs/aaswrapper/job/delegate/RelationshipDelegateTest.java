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
package org.eclipse.tractusx.irs.aaswrapper.job.delegate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tractusx.irs.SemanticModelNames.SINGLE_LEVEL_BOM_AS_BUILT_2_0_0;
import static org.eclipse.tractusx.irs.SemanticModelNames.SINGLE_LEVEL_BOM_AS_BUILT_3_0_0;
import static org.eclipse.tractusx.irs.SemanticModelNames.SINGLE_LEVEL_BOM_AS_BUILT_3_1_0;
import static org.eclipse.tractusx.irs.SemanticModelNames.SINGLE_LEVEL_BOM_AS_PLANNED_2_0_0;
import static org.eclipse.tractusx.irs.SemanticModelNames.SINGLE_LEVEL_BOM_AS_PLANNED_3_0_0;
import static org.eclipse.tractusx.irs.SemanticModelNames.SINGLE_LEVEL_BOM_AS_PLANNED_3_1_0;
import static org.eclipse.tractusx.irs.SemanticModelNames.SINGLE_LEVEL_USAGE_AS_BUILT_2_0_0;
import static org.eclipse.tractusx.irs.SemanticModelNames.SINGLE_LEVEL_USAGE_AS_BUILT_3_0_0;
import static org.eclipse.tractusx.irs.SemanticModelNames.SINGLE_LEVEL_USAGE_AS_BUILT_3_1_0;
import static org.eclipse.tractusx.irs.SemanticModelNames.SINGLE_LEVEL_USAGE_AS_PLANNED_2_0_0;
import static org.eclipse.tractusx.irs.SemanticModelNames.SINGLE_LEVEL_USAGE_AS_PLANNED_2_1_0;
import static org.eclipse.tractusx.irs.util.TestMother.jobParameter;
import static org.eclipse.tractusx.irs.util.TestMother.jobParameterDownwardAsPlanned;
import static org.eclipse.tractusx.irs.util.TestMother.jobParameterUpward;
import static org.eclipse.tractusx.irs.util.TestMother.jobParameterUpwardAsPlanned;
import static org.eclipse.tractusx.irs.util.TestMother.shell;
import static org.eclipse.tractusx.irs.util.TestMother.shellDescriptor;
import static org.eclipse.tractusx.irs.util.TestMother.submodelDescriptorWithDspEndpoint;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.eclipse.tractusx.irs.aaswrapper.job.AASTransferProcess;
import org.eclipse.tractusx.irs.aaswrapper.job.ItemContainer;
import org.eclipse.tractusx.irs.component.JobParameter;
import org.eclipse.tractusx.irs.component.PartChainIdentificationKey;
import org.eclipse.tractusx.irs.component.enums.ProcessStep;
import org.eclipse.tractusx.irs.edc.client.EdcSubmodelFacade;
import org.eclipse.tractusx.irs.edc.client.exceptions.EdcClientException;
import org.eclipse.tractusx.irs.edc.client.exceptions.UsagePolicyException;
import org.eclipse.tractusx.irs.edc.client.model.SubmodelDescriptor;
import org.eclipse.tractusx.irs.registryclient.discovery.ConnectorEndpointsService;
import org.eclipse.tractusx.irs.util.JsonUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RelationshipDelegateTest {

    final EdcSubmodelFacade submodelFacade = mock(EdcSubmodelFacade.class);
    final ConnectorEndpointsService connectorEndpointsService = mock(ConnectorEndpointsService.class);
    final JsonUtil jsonUtil = new JsonUtil();
    final RelationshipDelegate relationshipDelegate = new RelationshipDelegate(null, submodelFacade,
            connectorEndpointsService, jsonUtil);

    @Test
    void shouldFillItemContainerWithRelationshipAndAddChildIdsToProcess()
            throws EdcClientException, URISyntaxException, IOException {
        // given
        final String payload = Files.readString(
                Paths.get(Objects.requireNonNull(getClass().getResource("/singleLevelBomAsBuilt.json")).toURI()));
        when(submodelFacade.getSubmodelPayload(anyString(), anyString(), anyString(), any())).thenReturn(
                new SubmodelDescriptor("cid", payload));
        when(connectorEndpointsService.fetchConnectorEndpoints(any())).thenReturn(List.of("http://localhost"));

        final ItemContainer.ItemContainerBuilder itemContainerWithShell = ItemContainer.builder()
                                                                                       .shell(shell("", shellDescriptor(
                                                                                               List.of(submodelDescriptorWithDspEndpoint(
                                                                                                       SINGLE_LEVEL_BOM_AS_BUILT_3_0_0,
                                                                                                       "address")))));
        final AASTransferProcess aasTransferProcess = new AASTransferProcess();

        // when
        final ItemContainer result = relationshipDelegate.process(itemContainerWithShell, jobParameter(),
                aasTransferProcess, createKey());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getRelationships()).isNotEmpty();
        assertThat(aasTransferProcess.getIdsToProcess()).isNotEmpty();
        assertThat(aasTransferProcess.getIdsToProcess().get(0).getGlobalAssetId()).isNotEmpty();
    }

    @Test
    void shouldFillItemContainerWithUpwardRelationshipAndAddChildIdsToProcess()
            throws EdcClientException, URISyntaxException, IOException {
        // given
        final String payload = Files.readString(
                Paths.get(Objects.requireNonNull(getClass().getResource("/singleLevelUsageAsBuilt.json")).toURI()));
        when(submodelFacade.getSubmodelPayload(anyString(), anyString(), anyString(), any())).thenReturn(
                new SubmodelDescriptor("cid", payload));
        when(connectorEndpointsService.fetchConnectorEndpoints(any())).thenReturn(List.of("http://localhost"));

        final ItemContainer.ItemContainerBuilder itemContainerWithShell = ItemContainer.builder()
                                                                                       .shell(shell("", shellDescriptor(
                                                                                               List.of(submodelDescriptorWithDspEndpoint(
                                                                                                       SINGLE_LEVEL_USAGE_AS_BUILT_2_0_0,
                                                                                                       "address")))));
        final AASTransferProcess aasTransferProcess = new AASTransferProcess();

        // when
        final ItemContainer result = relationshipDelegate.process(itemContainerWithShell, jobParameterUpward(),
                aasTransferProcess, createKey());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getRelationships()).isNotEmpty();
        assertThat(aasTransferProcess.getIdsToProcess()).isNotEmpty();
        assertThat(aasTransferProcess.getIdsToProcess().get(0).getGlobalAssetId()).isNotEmpty();
        assertThat(aasTransferProcess.getIdsToProcess().get(0).getBpn()).isNotEmpty();
    }

    @Test
    void shouldFillItemContainerWithUpwardAsPlannedRelationshipAndAddChildIdsToProcess()
            throws EdcClientException, URISyntaxException, IOException {
        // given
        final String payload = Files.readString(Paths.get(
                Objects.requireNonNull(getClass().getResource("/relationships/singleLevelUsageAsPlanned-2.0.0.json"))
                       .toURI()));
        when(submodelFacade.getSubmodelPayload(anyString(), anyString(), anyString(), any())).thenReturn(
                new SubmodelDescriptor("cid", payload));
        when(connectorEndpointsService.fetchConnectorEndpoints(any())).thenReturn(List.of("http://localhost"));

        final ItemContainer.ItemContainerBuilder itemContainerWithShell = ItemContainer.builder()
                                                                                       .shell(shell("", shellDescriptor(
                                                                                               List.of(submodelDescriptorWithDspEndpoint(
                                                                                                       SINGLE_LEVEL_USAGE_AS_PLANNED_2_0_0,
                                                                                                       "address")))));
        final AASTransferProcess aasTransferProcess = new AASTransferProcess();

        // when
        final ItemContainer result = relationshipDelegate.process(itemContainerWithShell, jobParameterUpwardAsPlanned(),
                aasTransferProcess, createKey());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getRelationships()).isNotEmpty();
        assertThat(aasTransferProcess.getIdsToProcess()).isNotEmpty();
        assertThat(aasTransferProcess.getIdsToProcess().get(0).getGlobalAssetId()).isNotEmpty();
        assertThat(aasTransferProcess.getIdsToProcess().get(0).getBpn()).isNotEmpty();
    }

    @ParameterizedTest
    @MethodSource("relationshipParameters")
    void shouldFillItemContainerWithSupportedRelationshipAndAddChildIdsToProcess(final String relationshipFile,
            final String aspectName, final JobParameter jobParameter) throws Exception {
        // given
        final String payload = Files.readString(Paths.get(
                Objects.requireNonNull(getClass().getResource("/relationships/" + relationshipFile)).toURI()));
        when(submodelFacade.getSubmodelPayload(anyString(), anyString(), anyString(), any())).thenReturn(
                new SubmodelDescriptor("cid", payload));
        when(connectorEndpointsService.fetchConnectorEndpoints(any())).thenReturn(List.of("http://localhost"));

        final ItemContainer.ItemContainerBuilder itemContainerWithShell = ItemContainer.builder()
                                                                                       .shell(shell("", shellDescriptor(
                                                                                               List.of(submodelDescriptorWithDspEndpoint(
                                                                                                       aspectName,
                                                                                                       "address")))));
        final AASTransferProcess aasTransferProcess = new AASTransferProcess();

        // when
        final ItemContainer result = relationshipDelegate.process(itemContainerWithShell, jobParameter,
                aasTransferProcess, createKey());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getRelationships()).isNotEmpty();
        assertThat(aasTransferProcess.getIdsToProcess()).isNotEmpty();
        assertThat(aasTransferProcess.getIdsToProcess().get(0).getGlobalAssetId()).isNotEmpty();
        assertThat(aasTransferProcess.getIdsToProcess().get(0).getBpn()).isNotEmpty();
    }

    private static Stream<Arguments> relationshipParameters() {
        return Stream.of(Arguments.of("singleLevelUsageAsPlanned-2.0.0.json", SINGLE_LEVEL_USAGE_AS_PLANNED_2_0_0,
                        jobParameterUpwardAsPlanned()),
                Arguments.of("singleLevelUsageAsBuilt-3.0.0.json", SINGLE_LEVEL_USAGE_AS_BUILT_3_0_0,
                        jobParameterUpward()),
                Arguments.of("singleLevelBomAsBuilt-3.0.0.json", SINGLE_LEVEL_BOM_AS_BUILT_3_0_0, jobParameter()),
                Arguments.of("singleLevelBomAsPlanned-3.0.0.json", SINGLE_LEVEL_BOM_AS_PLANNED_3_0_0,
                        jobParameterDownwardAsPlanned())
                // asSpecified currently has no field for BPN of the child part
                // Arguments.of("singleLevelBomAsSpecified-2.0.0.json", SINGLE_LEVEL_BOM_AS_SPECIFIED_2_0_0,
                //         jobParameterDownwardAsSpecified()),
                // Arguments.of("singleLevelBomAsSpecified-1.0.0.json", SINGLE_LEVEL_BOM_AS_SPECIFIED_1_0_0,
                //         jobParameterDownwardAsSpecified())
        );
    }

    @ParameterizedTest
    @MethodSource("relationshipParametersFutureVersions")
    void shouldFillItemContainerWithPotentialFutureMinorVersions(final String relationshipFile, final String aspectName,
            final JobParameter jobParameter) throws Exception {
        // given
        final String payload = Files.readString(Paths.get(
                Objects.requireNonNull(getClass().getResource("/relationships/futureVersions/" + relationshipFile))
                       .toURI()));
        when(submodelFacade.getSubmodelPayload(anyString(), anyString(), anyString(), any())).thenReturn(
                new SubmodelDescriptor("cid", payload));
        when(connectorEndpointsService.fetchConnectorEndpoints(any())).thenReturn(List.of("http://localhost"));

        final ItemContainer.ItemContainerBuilder itemContainerWithShell = ItemContainer.builder()
                                                                                       .shell(shell("", shellDescriptor(
                                                                                               List.of(submodelDescriptorWithDspEndpoint(
                                                                                                       aspectName,
                                                                                                       "address")))));
        final AASTransferProcess aasTransferProcess = new AASTransferProcess();

        // when
        final ItemContainer result = relationshipDelegate.process(itemContainerWithShell, jobParameter,
                aasTransferProcess, createKey());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getRelationships()).isNotEmpty();
        assertThat(aasTransferProcess.getIdsToProcess()).isNotEmpty();
        assertThat(aasTransferProcess.getIdsToProcess().get(0).getGlobalAssetId()).isNotEmpty();
        assertThat(aasTransferProcess.getIdsToProcess().get(0).getBpn()).isNotEmpty();
    }

    public static Stream<Arguments> relationshipParametersFutureVersions() {
        return Stream.of(Arguments.of("singleLevelUsageAsPlanned-2.1.0.json", SINGLE_LEVEL_USAGE_AS_PLANNED_2_1_0,
                        jobParameterUpwardAsPlanned()),
                Arguments.of("singleLevelUsageAsBuilt-3.1.0.json", SINGLE_LEVEL_USAGE_AS_BUILT_3_1_0,
                        jobParameterUpward()),
                Arguments.of("singleLevelBomAsBuilt-3.1.0.json", SINGLE_LEVEL_BOM_AS_BUILT_3_1_0, jobParameter()),
                Arguments.of("singleLevelBomAsPlanned-3.1.0.json", SINGLE_LEVEL_BOM_AS_PLANNED_3_1_0,
                        jobParameterDownwardAsPlanned())
                // asSpecified currently has no field for BPN of the child part
                // Arguments.of("singleLevelBomAsSpecified-2.1.0.json", SINGLE_LEVEL_BOM_AS_SPECIFIED_2_1_0,
                //         jobParameterDownwardAsSpecified()),
                // Arguments.of("singleLevelBomAsSpecified-1.1.0.json", SINGLE_LEVEL_BOM_AS_SPECIFIED_1_1_0,
                //         jobParameterDownwardAsSpecified())
        );
    }

    @ParameterizedTest
    @MethodSource("relationshipParametersPreviousVersions")
    void shouldFillItemContainerWithPreviousVersions(final String relationshipFile, final String aspectName,
            final JobParameter jobParameter) throws Exception {
        // given
        final String payload = Files.readString(Paths.get(
                Objects.requireNonNull(getClass().getResource("/relationships/previous/" + relationshipFile)).toURI()));
        when(submodelFacade.getSubmodelPayload(anyString(), anyString(), anyString(), any())).thenReturn(
                new SubmodelDescriptor("cid", payload));
        when(connectorEndpointsService.fetchConnectorEndpoints(any())).thenReturn(List.of("http://localhost"));

        final ItemContainer.ItemContainerBuilder itemContainerWithShell = ItemContainer.builder()
                                                                                       .shell(shell("", shellDescriptor(
                                                                                               List.of(submodelDescriptorWithDspEndpoint(
                                                                                                       aspectName,
                                                                                                       "address")))));
        final AASTransferProcess aasTransferProcess = new AASTransferProcess();

        // when
        final ItemContainer result = relationshipDelegate.process(itemContainerWithShell, jobParameter,
                aasTransferProcess, createKey());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getRelationships()).isNotEmpty();
        assertThat(aasTransferProcess.getIdsToProcess()).isNotEmpty();
        assertThat(aasTransferProcess.getIdsToProcess().get(0).getGlobalAssetId()).isNotEmpty();
        assertThat(aasTransferProcess.getIdsToProcess().get(0).getBpn()).isNotEmpty();
    }

    public static Stream<Arguments> relationshipParametersPreviousVersions() {
        return Stream.of(
                Arguments.of("singleLevelBomAsBuilt-2.0.0.json", SINGLE_LEVEL_BOM_AS_BUILT_2_0_0, jobParameter()),
                Arguments.of("SingleLevelBomAsPlanned-2.0.0.json", SINGLE_LEVEL_BOM_AS_PLANNED_2_0_0,
                        jobParameterDownwardAsPlanned()));
    }

    @Test
    void shouldPutTombstoneForMissingBpn() {
        final ItemContainer.ItemContainerBuilder itemContainerWithShell = ItemContainer.builder()
                                                                                       .shell(shell("", shellDescriptor(
                                                                                               List.of(submodelDescriptorWithDspEndpoint(
                                                                                                       SINGLE_LEVEL_BOM_AS_BUILT_3_0_0,
                                                                                                       "address")))));
        // when
        final ItemContainer result = relationshipDelegate.process(itemContainerWithShell, jobParameter(),
                new AASTransferProcess(), PartChainIdentificationKey.builder().globalAssetId("testId").build());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTombstones()).hasSize(1);
        assertThat(result.getTombstones().get(0).getCatenaXId()).isEqualTo("testId");
        assertThat(result.getTombstones().get(0).getProcessingError().getProcessStep()).isEqualTo(
                ProcessStep.SUBMODEL_REQUEST);
    }

    @Test
    void shouldCatchRestClientExceptionAndPutTombstone() throws EdcClientException {
        // given
        when(submodelFacade.getSubmodelPayload(anyString(), anyString(), anyString(), any())).thenThrow(
                new EdcClientException("Unable to call endpoint"));
        when(connectorEndpointsService.fetchConnectorEndpoints(any())).thenReturn(List.of("http://localhost"));

        final ItemContainer.ItemContainerBuilder itemContainerWithShell = ItemContainer.builder()
                                                                                       .shell(shell("", shellDescriptor(
                                                                                               List.of(submodelDescriptorWithDspEndpoint(
                                                                                                       SINGLE_LEVEL_BOM_AS_BUILT_3_0_0,
                                                                                                       "address")))));

        // when
        final ItemContainer result = relationshipDelegate.process(itemContainerWithShell, jobParameter(),
                new AASTransferProcess(), createKey());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTombstones()).hasSize(1);
        assertThat(result.getTombstones().get(0).getCatenaXId()).isEqualTo("itemId");
        assertThat(result.getTombstones().get(0).getProcessingError().getProcessStep()).isEqualTo(
                ProcessStep.SUBMODEL_REQUEST);
    }

    @Test
    void shouldCatchJsonParseExceptionAndPutTombstone() throws EdcClientException {
        // given
        when(submodelFacade.getSubmodelPayload(anyString(), anyString(), anyString(), any())).thenThrow(
                new EdcClientException(new Exception("Payload did not match expected submodel")));
        when(connectorEndpointsService.fetchConnectorEndpoints(any())).thenReturn(List.of("http://localhost"));
        final ItemContainer.ItemContainerBuilder itemContainerWithShell = ItemContainer.builder()
                                                                                       .shell(shell("", shellDescriptor(
                                                                                               List.of(submodelDescriptorWithDspEndpoint(
                                                                                                       SINGLE_LEVEL_BOM_AS_BUILT_3_0_0,
                                                                                                       "address")))));

        // when
        final ItemContainer result = relationshipDelegate.process(itemContainerWithShell, jobParameter(),
                new AASTransferProcess(), createKey());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTombstones()).hasSize(1);
        assertThat(result.getTombstones().get(0).getCatenaXId()).isEqualTo("itemId");
        assertThat(result.getTombstones().get(0).getProcessingError().getProcessStep()).isEqualTo(
                ProcessStep.SUBMODEL_REQUEST);
    }

    @Test
    void shouldCatchUsagePolicyExceptionAndPutTombstone() throws EdcClientException {
        // given
        final String businessPartnerNumber = "BPNL000000011111";
        final ItemContainer.ItemContainerBuilder itemContainerWithShell = ItemContainer.builder()
                                                                                       .shell(shell("", shellDescriptor(
                                                                                               List.of(submodelDescriptorWithDspEndpoint(
                                                                                                       SINGLE_LEVEL_BOM_AS_BUILT_3_0_0,
                                                                                                       "address")))));

        // when
        when(submodelFacade.getSubmodelPayload(any(), any(), any(), any())).thenThrow(
                new UsagePolicyException("itemId", null, businessPartnerNumber));
        when(connectorEndpointsService.fetchConnectorEndpoints(any())).thenReturn(List.of("connector.endpoint.nl"));
        final ItemContainer result = relationshipDelegate.process(itemContainerWithShell, jobParameter(),
                new AASTransferProcess(), createKey());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTombstones()).hasSize(1);
        assertThat(result.getTombstones().get(0).getCatenaXId()).isEqualTo("itemId");
        assertThat(result.getTombstones().get(0).getBusinessPartnerNumber()).isEqualTo(businessPartnerNumber);
        assertThat(result.getTombstones().get(0).getProcessingError().getProcessStep()).isEqualTo(
                ProcessStep.USAGE_POLICY_VALIDATION);
    }

    private static PartChainIdentificationKey createKey() {
        return PartChainIdentificationKey.builder().globalAssetId("itemId").bpn("bpn123").build();
    }

}
