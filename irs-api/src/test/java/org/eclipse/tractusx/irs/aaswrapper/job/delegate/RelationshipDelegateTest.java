package org.eclipse.tractusx.irs.aaswrapper.job.delegate;

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
import org.eclipse.tractusx.irs.aaswrapper.submodel.domain.SubmodelFacade;
import org.eclipse.tractusx.irs.dto.JobParameter;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientException;

class RelationshipDelegateTest {

    final SubmodelFacade submodelFacade = mock(SubmodelFacade.class);
    final RelationshipDelegate relationshipDelegate = new RelationshipDelegate(null, submodelFacade);

    final String assemblyPartRelationshipAspectName = "urn:bamm:com.catenax.assembly_part_relationship:1.0.0#AssemblyPartRelationship";

    @Test
    void shouldFillItemContainerWithRelationshipAndAddChildIdsToProcess() {
        // given
        when(submodelFacade.getRelationships(anyString(), any())).thenReturn(Collections.singletonList(relationship()));
        final ItemContainer.ItemContainerBuilder itemContainerWithShell = ItemContainer.builder().shell(shellDescriptor(
                List.of(submodelDescriptor(assemblyPartRelationshipAspectName, "address"))));
        final AASTransferProcess aasTransferProcess = new AASTransferProcess();

        // when
        final ItemContainer result = relationshipDelegate.process(itemContainerWithShell, new JobParameter(),
                aasTransferProcess, "itemId");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getRelationships()).isNotEmpty();
        assertThat(aasTransferProcess.getIdsToProcess()).isNotEmpty();
    }

    @Test
    void shouldCatchRestClientExceptionAndPutTombstone() {
        // given
        when(submodelFacade.getRelationships(anyString(), any())).thenThrow(
                new RestClientException("Unable to call endpoint"));
        final ItemContainer.ItemContainerBuilder itemContainerWithShell = ItemContainer.builder().shell(shellDescriptor(
                List.of(submodelDescriptor(assemblyPartRelationshipAspectName, "address"))));

        // when
        final ItemContainer result = relationshipDelegate.process(itemContainerWithShell, new JobParameter(),
                new AASTransferProcess(), "itemId");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTombstones()).hasSize(1);
        assertThat(result.getTombstones().get(0).getCatenaXId()).isEqualTo("itemId");
    }

}
