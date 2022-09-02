package net.catenax.irs.aaswrapper.job.delegate;

import static net.catenax.irs.util.TestMother.shellDescriptor;
import static net.catenax.irs.util.TestMother.submodelDescriptorWithoutEndpoint;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import net.catenax.irs.aaswrapper.job.AASTransferProcess;
import net.catenax.irs.aaswrapper.job.ItemContainer;
import net.catenax.irs.aaswrapper.registry.domain.DigitalTwinRegistryFacade;
import net.catenax.irs.dto.JobParameter;
import org.junit.jupiter.api.Test;

class DigitalTwinDelegateTest {

    final DigitalTwinRegistryFacade digitalTwinRegistryFacade = mock(DigitalTwinRegistryFacade.class);
    final DigitalTwinDelegate digitalTwinDelegate = new DigitalTwinDelegate(null, digitalTwinRegistryFacade);

    @Test
    void shouldFillItemContainerWithShell() {
        // given
        when(digitalTwinRegistryFacade.getAAShellDescriptor(anyString())).thenReturn(shellDescriptor(
                List.of(submodelDescriptorWithoutEndpoint("any"))));

        // when
        final ItemContainer result = digitalTwinDelegate.process(ItemContainer.builder(), new JobParameter(),
                new AASTransferProcess(), "itemId");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getShells()).isNotEmpty();
    }

}
