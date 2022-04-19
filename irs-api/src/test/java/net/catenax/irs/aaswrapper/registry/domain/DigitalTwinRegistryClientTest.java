//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.aaswrapper.registry.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.List;

import feign.FeignException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@Disabled
@ActiveProfiles(profiles = { "local",
                             "prod"
})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DigitalTwinRegistryClientTest {

    @Autowired
    private DigitalTwinRegistryClient digitalTwinRegistryClient;

    @Autowired
    private DigitalTwinRegistryFacade digitalTwinRegistryFacade;

    @Test
    void shouldThrowExceptionWhenRequestingClientLocally() {
        assertThatExceptionOfType(FeignException.class).isThrownBy(
                () -> digitalTwinRegistryClient.getAssetAdministrationShellDescriptor("test"));

    }

    @Test
    void shouldReturnTombstoneWhenCallingFacadeWithFeignClientLocally() {
        final List<AbstractAasShell> shell = digitalTwinRegistryFacade.getAASSubmodelEndpoint("test");
        assertThat(shell).hasSize(1);
        final AbstractAasShell abstractAasShell = shell.get(0);
        assertThat(abstractAasShell.getIdentification()).isEqualTo("test");
        assertThat(abstractAasShell).isInstanceOf(AasShellTombstone.class);
        final AasShellTombstone tombstone = (AasShellTombstone) abstractAasShell;
        assertThat(tombstone.getProcessingError().getException()).isPresent();
        assertThat(tombstone.getProcessingError().getException().get()).isInstanceOf(FeignException.class);
    }

}