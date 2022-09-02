package net.catenax.irs.aaswrapper.job.delegate;

import static net.catenax.irs.util.TestMother.shellDescriptor;
import static net.catenax.irs.util.TestMother.submodelDescriptorWithoutEndpoint;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import net.catenax.irs.aaswrapper.job.AASTransferProcess;
import net.catenax.irs.aaswrapper.job.ItemContainer;
import net.catenax.irs.bpdm.BpdmFacade;
import net.catenax.irs.dto.JobParameter;
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
