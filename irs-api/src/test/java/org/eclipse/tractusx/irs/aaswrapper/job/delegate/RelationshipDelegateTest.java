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

import static org.eclipse.tractusx.irs.util.TestMother.jobParameter;
import static org.eclipse.tractusx.irs.util.TestMother.relationship;
import static org.eclipse.tractusx.irs.util.TestMother.shellDescriptor;
import static org.eclipse.tractusx.irs.util.TestMother.submodelDescriptor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.eclipse.tractusx.irs.aaswrapper.job.AASTransferProcess;
import org.eclipse.tractusx.irs.aaswrapper.job.ItemContainer;
import org.eclipse.tractusx.irs.component.enums.ProcessStep;
import org.eclipse.tractusx.irs.edc.client.EdcSubmodelFacade;
import org.eclipse.tractusx.irs.edc.client.exceptions.EdcClientException;
import org.junit.jupiter.api.Test;

class RelationshipDelegateTest {

    final EdcSubmodelFacade submodelFacade = mock(EdcSubmodelFacade.class);
    final RelationshipDelegate relationshipDelegate = new RelationshipDelegate(null, submodelFacade);

    final String singleLevelBomAsBuiltAspectName = "urn:bamm:com.catenax.single_level_bom_as_built:1.0.0#SingleLevelBomAsBuilt";

    @Test
    void shouldFillItemContainerWithRelationshipAndAddChildIdsToProcess() throws EdcClientException {
        // given
        when(submodelFacade.getRelationships(anyString(), any())).thenReturn(Collections.singletonList(relationship()));
        final ItemContainer.ItemContainerBuilder itemContainerWithShell = ItemContainer.builder().shell(shellDescriptor(
                List.of(submodelDescriptor(singleLevelBomAsBuiltAspectName, "address"))));
        final AASTransferProcess aasTransferProcess = new AASTransferProcess();

        // when
        final ItemContainer result = relationshipDelegate.process(itemContainerWithShell, jobParameter(),
                aasTransferProcess, "itemId");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getRelationships()).isNotEmpty();
        assertThat(aasTransferProcess.getIdsToProcess()).isNotEmpty();
    }

    @Test
    void shouldCatchRestClientExceptionAndPutTombstone() throws EdcClientException {
        // given
        when(submodelFacade.getRelationships(anyString(), any())).thenThrow(
                new EdcClientException("Unable to call endpoint"));
        final ItemContainer.ItemContainerBuilder itemContainerWithShell = ItemContainer.builder().shell(shellDescriptor(
                List.of(submodelDescriptor(singleLevelBomAsBuiltAspectName, "address"))));

        // when
        final ItemContainer result = relationshipDelegate.process(itemContainerWithShell, jobParameter(),
                new AASTransferProcess(), "itemId");

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
        when(submodelFacade.getRelationships(anyString(), any())).thenThrow(
                new EdcClientException(new Exception("Payload did not match expected submodel")));
        final ItemContainer.ItemContainerBuilder itemContainerWithShell = ItemContainer.builder().shell(shellDescriptor(
                List.of(submodelDescriptor(singleLevelBomAsBuiltAspectName, "address"))));

        // when
        final ItemContainer result = relationshipDelegate.process(itemContainerWithShell, jobParameter(),
                new AASTransferProcess(), "itemId");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTombstones()).hasSize(1);
        assertThat(result.getTombstones().get(0).getCatenaXId()).isEqualTo("itemId");
        assertThat(result.getTombstones().get(0).getProcessingError().getProcessStep()).isEqualTo(
                ProcessStep.SUBMODEL_REQUEST);
    }

}
