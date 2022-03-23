package net.catenax.irs.aaswrapper.registry.domain;

import static org.assertj.core.api.Assertions.assertThat;

import net.catenax.irs.entities.EntitiesMother;
import net.catenax.irs.mappers.PartAspectEntityToDtoMapper;
import org.junit.jupiter.api.Test;

class DigitalTwinRegistryClientLocalTests {

    DigitalTwinRegistryClientLocalStub digitalTwinRegistryClientLocalStub = new DigitalTwinRegistryClientLocalStub();

    @Test
    void toAspect() {
        var input = digitalTwinRegistryClientLocalStub.getAssetAdministrationShellDescriptor("aa");

        assertThat(input.getIdentification()).isEqualTo("identification");

    }
}
