package org.eclipse.tractusx.esr.irs.model.shell;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tractusx.esr.irs.IrsFixture.exampleShell;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class ShellTest {

    @Test
    void shouldFindValidBpnValue() {
        // given
        final Shell shellWithBpn = exampleShell();

        // when
        final Optional<String> manufacturerId = shellWithBpn.findManufacturerIdIfValid();

        // then
        assertThat(manufacturerId).isNotEmpty();
    }

    @Test
    void shouldNotFindBpnWhenValueIsNotValid() {
        // given
        final String wrongBPNValue = "WRONG_BPN";
        final Shell shellWithWrongBpn = Shell.builder()
                        .identification("urn:uuid:4ad4a1ce-beb2-42d2-bfe7-d5d9c68d6daf")
                        .specificAssetIds(List.of(IdentifierKeyValuePair.builder()
                                                                        .key("ManufacturerId")
                                                                        .value(wrongBPNValue)
                                                                        .build()))
                        .build();

        // when
        final Optional<String> manufacturerId = shellWithWrongBpn.findManufacturerIdIfValid();

        // then
        assertThat(manufacturerId).isEmpty();
    }

    @Test
    void shouldNotFindBpnWhenManufacturerIdIsMissingInSpecificAssetIds() {
        // given
        final Shell shellWithWrongBpn = Shell.builder()
                                             .identification("urn:uuid:4ad4a1ce-beb2-42d2-bfe7-d5d9c68d6daf")
                                             .specificAssetIds(List.of(IdentifierKeyValuePair.builder()
                                                                                             .key("Key")
                                                                                             .value("Value")
                                                                                             .build()))
                                             .build();

        // when
        final Optional<String> manufacturerId = shellWithWrongBpn.findManufacturerIdIfValid();

        // then
        assertThat(manufacturerId).isEmpty();
    }

}