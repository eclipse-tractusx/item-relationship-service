package org.eclipse.tractusx.irs.aaswrapper.job.delegate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tractusx.irs.util.TestMother.shellDescriptor;
import static org.eclipse.tractusx.irs.util.TestMother.submodelDescriptorWithoutEndpoint;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.eclipse.tractusx.irs.aaswrapper.job.AASTransferProcess;
import org.eclipse.tractusx.irs.aaswrapper.job.ItemContainer;
import org.eclipse.tractusx.irs.bpdm.BpdmFacade;
import org.eclipse.tractusx.irs.dto.JobParameter;
import org.junit.jupiter.api.Test;

class BpdmDelegateTest {

    final BpdmFacade bpdmFacade = mock(BpdmFacade.class);
    final BpdmDelegate bpdmDelegate = new BpdmDelegate(bpdmFacade);

    @Test
    void shouldFillItemContainerWithBpn() {
        // given
        when(bpdmFacade.findManufacturerName(any())).thenReturn(Optional.of("Tier A"));
        final ItemContainer.ItemContainerBuilder itemContainerWithShell = ItemContainer.builder().shell(shellDescriptor(
                List.of(submodelDescriptorWithoutEndpoint("any"))));

        // when
        final ItemContainer result = bpdmDelegate.process(itemContainerWithShell, new JobParameter(),
                new AASTransferProcess(), "itemId");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getBpns()).isNotEmpty();
        assertThat(result.getBpns().stream().findFirst().get().getManufacturerName()).isEqualTo("Tier A");
    }

}
