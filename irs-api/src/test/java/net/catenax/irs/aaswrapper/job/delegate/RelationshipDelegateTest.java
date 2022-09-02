package net.catenax.irs.aaswrapper.job.delegate;

import static net.catenax.irs.util.TestMother.relationship;
import static net.catenax.irs.util.TestMother.shellDescriptor;
import static net.catenax.irs.util.TestMother.submodelDescriptor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import net.catenax.irs.aaswrapper.job.AASTransferProcess;
import net.catenax.irs.aaswrapper.job.ItemContainer;
import net.catenax.irs.aaswrapper.submodel.domain.SubmodelFacade;
import net.catenax.irs.dto.JobParameter;
import org.junit.jupiter.api.Test;

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

}
