package net.catenax.irs.aaswrapper.registry.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import net.catenax.irs.dto.SubmodelEndpoint;
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
        final List<SubmodelEndpoint> aasSubmodelEndpointAddresses = digitalTwinRegistryFacade.getAASSubmodelEndpoints(
                catenaXId);
        assertThat(aasSubmodelEndpointAddresses).isNotNull().hasSize(1);
        assertThat(aasSubmodelEndpointAddresses.get(0).getAddress()).isEqualTo(catenaXId);
    }
}
