package org.eclipse.tractusx.irs.aaswrapper.job.delegate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tractusx.irs.util.TestMother.shellDescriptor;
import static org.eclipse.tractusx.irs.util.TestMother.submodelDescriptorWithoutEndpoint;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Optional;

import org.eclipse.tractusx.irs.aaswrapper.job.AASTransferProcess;
import org.eclipse.tractusx.irs.aaswrapper.job.ItemContainer;
import org.eclipse.tractusx.irs.bpdm.BpdmFacade;
import org.eclipse.tractusx.irs.component.assetadministrationshell.AssetAdministrationShellDescriptor;
import org.eclipse.tractusx.irs.component.assetadministrationshell.IdentifierKeyValuePair;
import org.eclipse.tractusx.irs.dto.JobParameter;
import org.junit.jupiter.api.Test;

class BpdmDelegateTest {

    final BpdmFacade bpdmFacade = mock(BpdmFacade.class);
    final BpdmDelegate bpdmDelegate = new BpdmDelegate(bpdmFacade);

    @Test
    void shouldFillItemContainerWithBpn() {
        // given
        given(bpdmFacade.findManufacturerName(any())).willReturn(Optional.of("Tier A"));
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

    @Test
    void shouldCreateTombstoneForMissingManufacturerId() {
        // given
        given(bpdmFacade.findManufacturerName(any())).willReturn(Optional.of("Tier A"));
        final ItemContainer.ItemContainerBuilder itemContainerWithShell = itemContainerWithAssetId("ManufaAAAA", "BPNL00000003AYRE");

        // when
        final ItemContainer result = bpdmDelegate.process(itemContainerWithShell, new JobParameter(),
                new AASTransferProcess(), "itemId");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getBpns()).isEmpty();
        assertThat(result.getTombstones()).hasSize(1);
        assertThat(result.getTombstones().get(0).getCatenaXId()).isEqualTo("itemId");
    }

    @Test
    void shouldCreateTombstoneForNotValidBpn() {
        // given
        given(bpdmFacade.findManufacturerName(any())).willReturn(Optional.of("Tier A"));
        final ItemContainer.ItemContainerBuilder itemContainerWithShell = itemContainerWithAssetId("ManufacturerId", "dsfvzsdfvzsvdszvzdsvdszvdszvds");

        // when
        final ItemContainer result = bpdmDelegate.process(itemContainerWithShell, new JobParameter(),
                new AASTransferProcess(), "itemId");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getBpns()).isEmpty();
        assertThat(result.getTombstones()).hasSize(1);
        assertThat(result.getTombstones().get(0).getCatenaXId()).isEqualTo("itemId");
    }

    @Test
    void shouldCreateTombstoneForMissingBpnForGivenManufacturerId() {
        // given
        final ItemContainer.ItemContainerBuilder itemContainerWithShell = itemContainerWithAssetId("ManufacturerId", "BPNL00000003AYRE");

        // when
        final ItemContainer result = bpdmDelegate.process(itemContainerWithShell, new JobParameter(),
                new AASTransferProcess(), "itemId");

        // then
        assertThat(result).isNotNull();
        assertThat(result.getBpns()).isEmpty();
        assertThat(result.getTombstones()).hasSize(1);
        assertThat(result.getTombstones().get(0).getCatenaXId()).isEqualTo("itemId");
    }

    private ItemContainer.ItemContainerBuilder itemContainerWithAssetId(String key, String value) {
        return ItemContainer.builder().shell(
                AssetAdministrationShellDescriptor
                        .builder()
                        .specificAssetIds(List.of(
                                IdentifierKeyValuePair.builder().key(key).value(value).build()))
                        .build());
    }

}
