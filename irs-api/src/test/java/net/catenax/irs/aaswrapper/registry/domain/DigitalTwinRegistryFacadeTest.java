package net.catenax.irs.aaswrapper.registry.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DigitalTwinRegistryFacadeTest {

    private DigitalTwinRegistryFacade digitalTwinRegistryFacade;

    @BeforeEach
    void setUp() {
        digitalTwinRegistryFacade = new DigitalTwinRegistryFacade(new DigitalTwinRegistryClientLocalStub());
    }

    @Test
    void shouldReturnSubmodelEndpointsWhenRequestingWithCatenaXId() {
        final String catenaXId = "8a61c8db-561e-4db0-84ec-a693fc5ffdf6";
        final List<AbstractAasShell> shellEndpoints = digitalTwinRegistryFacade.getAASSubmodelEndpoint(catenaXId);
        assertThat(shellEndpoints).isNotNull().hasSize(1);
        final AbstractAasShell shell = shellEndpoints.get(0);
        assertThat(shell).isInstanceOf(AasShellSubmodelDescriptor.class);
        final AasShellSubmodelDescriptor aasShellSubmodelDescriptor = (AasShellSubmodelDescriptor) shell;
        assertThat(aasShellSubmodelDescriptor.getSubmodelEndpointAddress()).isEqualTo(catenaXId);
    }
}
