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
package org.eclipse.tractusx.irs.aaswrapper.job.delegate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tractusx.irs.util.TestMother.jobParameterCollectAspects;
import static org.eclipse.tractusx.irs.util.TestMother.jobParameterFilter;
import static org.eclipse.tractusx.irs.util.TestMother.shellDescriptor;
import static org.eclipse.tractusx.irs.util.TestMother.submodelDescriptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.tractusx.irs.edc.client.EdcSubmodelFacade;
import org.eclipse.tractusx.irs.aaswrapper.job.AASTransferProcess;
import org.eclipse.tractusx.irs.aaswrapper.job.ItemContainer;
import org.eclipse.tractusx.irs.component.enums.ProcessStep;
import org.eclipse.tractusx.irs.data.JsonParseException;
import org.eclipse.tractusx.irs.edc.client.exceptions.EdcClientException;
import org.eclipse.tractusx.irs.edc.client.exceptions.UsagePolicyException;
import org.eclipse.tractusx.irs.semanticshub.SemanticsHubFacade;
import org.eclipse.tractusx.irs.services.validation.JsonValidatorService;
import org.eclipse.tractusx.irs.services.validation.SchemaNotFoundException;
import org.eclipse.tractusx.irs.util.JsonUtil;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientException;

class SubmodelDelegateTest {

    final EdcSubmodelFacade submodelFacade = mock(EdcSubmodelFacade.class);
    final SemanticsHubFacade semanticsHubFacade = mock(SemanticsHubFacade.class);
    final JsonValidatorService jsonValidatorService = mock(JsonValidatorService.class);
    final SubmodelDelegate submodelDelegate = new SubmodelDelegate(submodelFacade,
            semanticsHubFacade, jsonValidatorService, new JsonUtil());

    @Test
    void shouldFilterSubmodelDescriptorsByAspectTypeFilter() {
        // given
        final ItemContainer.ItemContainerBuilder itemContainerShellWithTwoSubmodels = ItemContainer.builder().shell(shellDescriptor(
                List.of(submodelDescriptor("urn:bamm:com.catenax.serial_part_typization:1.0.0#SerialPartTypization",
                                "testSerialPartTypizationEndpoint"),
                        submodelDescriptor("urn:bamm:com.catenax.assembly_part_relationship:1.0.0#AssemblyPartRelationship",
                                "testAssemblyPartRelationshipEndpoint"))));

        // when
        final ItemContainer result = submodelDelegate.process(itemContainerShellWithTwoSubmodels, jobParameterFilter(),
                new AASTransferProcess(), "itemId");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getShells().get(0).getSubmodelDescriptors()).isEmpty();
    }

    @Test
    void shouldCatchJsonParseExceptionAndPutTombstone() throws SchemaNotFoundException {
        // given
        final ItemContainer.ItemContainerBuilder itemContainerShellWithTwoSubmodels = ItemContainer.builder().shell(shellDescriptor(
                List.of(submodelDescriptor("urn:bamm:com.catenax.serial_part:1.0.0#SerialPart",
                                "testSerialPartEndpoint"),
                        submodelDescriptor("urn:bamm:com.catenax.single_level_bom_as_built:1.0.0#SingleLevelBomAsBuilt",
                                "testSingleLevelBomAsBuiltEndpoint"))));

        // when
        when(semanticsHubFacade.getModelJsonSchema(any())).thenThrow(
                new JsonParseException(new Exception("Payload did not match expected submodel")));
        final ItemContainer result = submodelDelegate.process(itemContainerShellWithTwoSubmodels, jobParameterCollectAspects(),
                new AASTransferProcess(), "itemId");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTombstones()).hasSize(2);
        assertThat(result.getTombstones().get(0).getCatenaXId()).isEqualTo("itemId");
        assertThat(result.getTombstones().get(0).getProcessingError().getProcessStep()).isEqualTo(
                ProcessStep.SCHEMA_VALIDATION);
    }

    @Test
    void shouldCatchUsagePolicyExceptionAndPutTombstone() throws EdcClientException {
        // given
        final ItemContainer.ItemContainerBuilder itemContainerShellWithTwoSubmodels = ItemContainer.builder().shell(shellDescriptor(
                List.of(submodelDescriptor("urn:bamm:com.catenax.serial_part:1.0.0#SerialPart",
                                "testSerialPartEndpoint"),
                        submodelDescriptor("urn:bamm:com.catenax.single_level_bom_as_built:1.0.0#SingleLevelBomAsBuilt",
                                "testSingleLevelBomAsBuiltEndpoint"))));

        // when
        when(submodelFacade.getSubmodelRawPayload(any())).thenThrow(new UsagePolicyException("itemId"));
        final ItemContainer result = submodelDelegate.process(itemContainerShellWithTwoSubmodels,
                jobParameterCollectAspects(), new AASTransferProcess(), "itemId");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTombstones()).hasSize(2);
        assertThat(result.getTombstones().get(0).getCatenaXId()).isEqualTo("itemId");
        assertThat(result.getTombstones().get(0).getProcessingError().getProcessStep()).isEqualTo(
                ProcessStep.USAGE_POLICY_VALIDATION);
    }

    @Test
    void shouldCatchRestClientExceptionAndPutTombstone() throws SchemaNotFoundException {
        // given
        final ItemContainer.ItemContainerBuilder itemContainerShellWithTwoSubmodels = ItemContainer.builder().shell(shellDescriptor(
                List.of(submodelDescriptor("urn:bamm:com.catenax.serial_part:1.0.0#SerialPart",
                                "testSerialPartEndpoint"),
                        submodelDescriptor("urn:bamm:com.catenax.single_level_bom_as_built:1.0.0#SingleLevelBomAsBuilt",
                                "testSingleLevelBomAsBuiltEndpoint"))));

        // when
        when(semanticsHubFacade.getModelJsonSchema(any())).thenThrow(
                new RestClientException("Payload did not match expected submodel"));
        final ItemContainer result = submodelDelegate.process(itemContainerShellWithTwoSubmodels, jobParameterCollectAspects(),
                new AASTransferProcess(), "itemId");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTombstones()).hasSize(2);
        assertThat(result.getTombstones().get(0).getCatenaXId()).isEqualTo("itemId");
        assertThat(result.getTombstones().get(0).getProcessingError().getProcessStep()).isEqualTo(
                ProcessStep.SCHEMA_REQUEST);
    }

}
