package net.catenax.irs.bpdm;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;

class BpdmFacadeTest {

    private final BpdmFacade bpdmFacade = new BpdmFacade(new BpdmClientLocalStub());

    @Test
    void shouldReturnManufacturerNameWhenManufacturerIdExists() {
        final String manufacturerId = "BPNL00000003AYRE";

        final Optional<String> manufacturerName = bpdmFacade.findManufacturerName(manufacturerId);

        assertThat(manufacturerName).isNotEmpty();
        assertThat(manufacturerName.get()).isEqualTo("OEM A");
    }

}
