package org.eclipse.tractusx.irs.aaswrapper.job.delegate;

import static org.eclipse.tractusx.irs.util.TestMother.shellDescriptor;
import static org.eclipse.tractusx.irs.util.TestMother.submodelDescriptorWithoutEndpoint;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.tractusx.irs.aaswrapper.job.AASTransferProcess;
import org.eclipse.tractusx.irs.aaswrapper.job.ItemContainer;
import org.eclipse.tractusx.irs.aaswrapper.registry.domain.DigitalTwinRegistryFacade;
import org.eclipse.tractusx.irs.dto.JobParameter;
import org.eclipse.tractusx.irs.aaswrapper.job.delegate.DigitalTwinDelegate;
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
