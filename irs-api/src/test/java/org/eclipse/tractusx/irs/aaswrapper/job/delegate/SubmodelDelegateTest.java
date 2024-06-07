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
import static org.eclipse.tractusx.irs.util.TestMother.jobParameterCollectAspects;
import static org.eclipse.tractusx.irs.util.TestMother.jobParameterFilter;
import static org.eclipse.tractusx.irs.SemanticModelNames.SERIAL_PART_3_0_0;
import static org.eclipse.tractusx.irs.util.TestMother.shell;
import static org.eclipse.tractusx.irs.util.TestMother.shellDescriptor;
import static org.eclipse.tractusx.irs.SemanticModelNames.SINGLE_LEVEL_BOM_AS_BUILT_3_0_0;
import static org.eclipse.tractusx.irs.util.TestMother.submodelDescriptor;
import static org.eclipse.tractusx.irs.util.TestMother.submodelDescriptorWithDspEndpoint;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.tractusx.irs.aaswrapper.job.AASTransferProcess;
import org.eclipse.tractusx.irs.aaswrapper.job.ItemContainer;
import org.eclipse.tractusx.irs.component.PartChainIdentificationKey;
import org.eclipse.tractusx.irs.component.enums.ProcessStep;
import org.eclipse.tractusx.irs.data.JsonParseException;
import org.eclipse.tractusx.irs.edc.client.EdcSubmodelFacade;
import org.eclipse.tractusx.irs.edc.client.ItemNotFoundInCatalogException;
import org.eclipse.tractusx.irs.edc.client.exceptions.EdcClientException;
import org.eclipse.tractusx.irs.edc.client.exceptions.UsagePolicyException;
import org.eclipse.tractusx.irs.edc.client.model.SubmodelDescriptor;
import org.eclipse.tractusx.irs.registryclient.discovery.ConnectorEndpointsService;
import org.eclipse.tractusx.irs.semanticshub.SemanticsHubFacade;
import org.eclipse.tractusx.irs.services.validation.InvalidSchemaException;
import org.eclipse.tractusx.irs.services.validation.JsonValidatorService;
import org.eclipse.tractusx.irs.services.validation.SchemaNotFoundException;
import org.eclipse.tractusx.irs.services.validation.ValidationResult;
import org.eclipse.tractusx.irs.util.JsonUtil;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientException;

class SubmodelDelegateTest {

    final EdcSubmodelFacade submodelFacade = mock(EdcSubmodelFacade.class);
    final SemanticsHubFacade semanticsHubFacade = mock(SemanticsHubFacade.class);
    final JsonValidatorService jsonValidatorService = mock(JsonValidatorService.class);
    final ConnectorEndpointsService connectorEndpointsService = mock(ConnectorEndpointsService.class);
    final SubmodelDelegate submodelDelegate = new SubmodelDelegate(submodelFacade, semanticsHubFacade,
            jsonValidatorService, new JsonUtil(), connectorEndpointsService);

    @Test
    void shouldNotFilterSubmodelDescriptorsByAspectTypeFilter() {
        // given
        final ItemContainer.ItemContainerBuilder itemContainerShellWithTwoSubmodels = ItemContainer.builder()
                                                                                                   .shell(shell("", shellDescriptor(
                                                                                                           List.of(submodelDescriptorWithDspEndpoint(
                                                                                                                           SERIAL_PART_3_0_0,
                                                                                                                           "testSerialPartTypizationEndpoint"),
                                                                                                                   submodelDescriptorWithDspEndpoint(
                                                                                                                           SINGLE_LEVEL_BOM_AS_BUILT_3_0_0,
                                                                                                                           "testAssemblyPartRelationshipEndpoint")))));

        // when
        final ItemContainer result = submodelDelegate.process(itemContainerShellWithTwoSubmodels, jobParameterFilter(),
                new AASTransferProcess(), createKey());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getShells().get(0).payload().getSubmodelDescriptors()).isNotEmpty();
    }

    @Test
    void shouldCatchJsonParseExceptionAndPutTombstone() throws SchemaNotFoundException {
        // given
        final ItemContainer.ItemContainerBuilder itemContainerShellWithTwoSubmodels = ItemContainer.builder()
                                                                                                   .shell(shell("", shellDescriptor(
                                                                                                           List.of(submodelDescriptorWithDspEndpoint(
                                                                                                                           SERIAL_PART_3_0_0,
                                                                                                                           "testSerialPartEndpoint"),
                                                                                                                   submodelDescriptorWithDspEndpoint(
                                                                                                                           SINGLE_LEVEL_BOM_AS_BUILT_3_0_0,
                                                                                                                           "testSingleLevelBomAsBuiltEndpoint")))));

        // when
        when(semanticsHubFacade.getModelJsonSchema(any())).thenThrow(
                new JsonParseException(new Exception("Payload did not match expected submodel")));
        final ItemContainer result = submodelDelegate.process(itemContainerShellWithTwoSubmodels,
                jobParameterCollectAspects(), new AASTransferProcess(), createKey());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTombstones()).hasSize(2);
        assertThat(result.getTombstones().get(0).getCatenaXId()).isEqualTo("itemId");
        assertThat(result.getTombstones().get(0).getProcessingError().getProcessStep()).isEqualTo(
                ProcessStep.SCHEMA_VALIDATION);
    }

    @Test
    void shouldPutTombstoneForMissingBpn() {
        final ItemContainer.ItemContainerBuilder itemContainerShellWithTwoSubmodels = ItemContainer.builder()
                                                                                                   .shell(shell("", shellDescriptor(
                                                                                                           List.of(submodelDescriptorWithDspEndpoint(
                                                                                                                           SERIAL_PART_3_0_0,
                                                                                                                           "testSerialPartEndpoint"),
                                                                                                                   submodelDescriptorWithDspEndpoint(
                                                                                                                           SINGLE_LEVEL_BOM_AS_BUILT_3_0_0,
                                                                                                                           "testSingleLevelBomAsBuiltEndpoint")))));

        // when
        final ItemContainer result = submodelDelegate.process(itemContainerShellWithTwoSubmodels,
                jobParameterCollectAspects(), new AASTransferProcess(),
                PartChainIdentificationKey.builder().globalAssetId("testId").build());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTombstones()).hasSize(2);
        assertThat(result.getTombstones().get(0).getCatenaXId()).isEqualTo("testId");
        assertThat(result.getTombstones().get(0).getProcessingError().getProcessStep()).isEqualTo(
                ProcessStep.SUBMODEL_REQUEST);
    }

    @Test
    void shouldCatchUsagePolicyExceptionAndPutTombstone() throws EdcClientException {
        // given
        final String businessPartnerNumber = "BPNL000000011111";
        final ItemContainer.ItemContainerBuilder itemContainerShellWithTwoSubmodels = ItemContainer.builder()
                                                                                                   .shell(shell("", shellDescriptor(
                                                                                                           List.of(submodelDescriptorWithDspEndpoint(
                                                                                                                           SERIAL_PART_3_0_0,
                                                                                                                           "testSerialPartEndpoint"),
                                                                                                                   submodelDescriptorWithDspEndpoint(
                                                                                                                           SINGLE_LEVEL_BOM_AS_BUILT_3_0_0,
                                                                                                                           "testSingleLevelBomAsBuiltEndpoint")))));

        // when
        when(submodelFacade.getSubmodelPayload(any(), any(), any(), any())).thenThrow(new UsagePolicyException("itemId", null, businessPartnerNumber));
        when(connectorEndpointsService.fetchConnectorEndpoints(any())).thenReturn(List.of("connector.endpoint.nl"));
        final ItemContainer result = submodelDelegate.process(itemContainerShellWithTwoSubmodels,
                jobParameterCollectAspects(), new AASTransferProcess(), createKey());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTombstones()).hasSize(2);
        assertThat(result.getTombstones().get(0).getCatenaXId()).isEqualTo("itemId");
        assertThat(result.getTombstones().get(0).getBusinessPartnerNumber()).isEqualTo(businessPartnerNumber);
        assertThat(result.getTombstones().get(0).getProcessingError().getProcessStep()).isEqualTo(
                ProcessStep.USAGE_POLICY_VALIDATION);
    }

    @Test
    void shouldRequestForAllEndpoints() throws EdcClientException, InvalidSchemaException {
        // given
        final ItemContainer.ItemContainerBuilder itemContainerShellWithOneSubmodel = ItemContainer.builder()
                                                                                                  .shell(shell("", shellDescriptor(
                                                                                                          List.of(submodelDescriptor(
                                                                                                                  SERIAL_PART_3_0_0,
                                                                                                                  "testSerialPartEndpoint",
                                                                                                                  "")))));

        // when
        when(submodelFacade.getSubmodelPayload(any(), any(), any(), any())).thenThrow(
                new ItemNotFoundInCatalogException("test", "itemId")).thenReturn(new SubmodelDescriptor("cid", """
                {"test": "test"}
                """));
        when(jsonValidatorService.validate(any(), any())).thenReturn(ValidationResult.builder().valid(true).build());
        when(connectorEndpointsService.fetchConnectorEndpoints(any())).thenReturn(
                List.of("connector.endpoint.n1", "connector.endpoint.n2"));
        final ItemContainer result = submodelDelegate.process(itemContainerShellWithOneSubmodel,
                jobParameterCollectAspects(), new AASTransferProcess(), createKey());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getSubmodels()).hasSize(1);
        assertThat(result.getSubmodels().get(0).getAspectType()).isEqualTo(SERIAL_PART_3_0_0);
        assertThat(result.getSubmodels().get(0).getContractAgreementId()).isNull();
        assertThat(result.getTombstones()).isEmpty();
    }

    @Test
    void shouldCatchRestClientExceptionAndPutTombstone() throws SchemaNotFoundException {
        // given
        final ItemContainer.ItemContainerBuilder itemContainerShellWithTwoSubmodels = ItemContainer.builder()
                                                                                                   .shell(shell("", shellDescriptor(
                                                                                                           List.of(submodelDescriptorWithDspEndpoint(
                                                                                                                           SERIAL_PART_3_0_0,
                                                                                                                           "testSerialPartEndpoint"),
                                                                                                                   submodelDescriptorWithDspEndpoint(
                                                                                                                           SINGLE_LEVEL_BOM_AS_BUILT_3_0_0,
                                                                                                                           "testSingleLevelBomAsBuiltEndpoint")))));

        // when
        when(semanticsHubFacade.getModelJsonSchema(any())).thenThrow(
                new RestClientException("Payload did not match expected submodel"));
        final ItemContainer result = submodelDelegate.process(itemContainerShellWithTwoSubmodels,
                jobParameterCollectAspects(), new AASTransferProcess(), createKey());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTombstones()).hasSize(2);
        assertThat(result.getTombstones().get(0).getCatenaXId()).isEqualTo("itemId");
        assertThat(result.getTombstones().get(0).getProcessingError().getProcessStep()).isEqualTo(
                ProcessStep.SCHEMA_REQUEST);
    }

    private static PartChainIdentificationKey createKey() {
        return PartChainIdentificationKey.builder().globalAssetId("itemId").bpn("bpn123").build();
    }

}
