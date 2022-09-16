package org.eclipse.tractusx.esr.irs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tractusx.esr.irs.IrsFixture.exampleRelationship;
import static org.eclipse.tractusx.esr.irs.IrsFixture.exampleShellWithGlobalAssetId;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.tractusx.esr.service.BpnData;
import org.junit.jupiter.api.Test;

class IrsResponseTest {

    @Test
    public void shouldFindBpnValueOfRequestor() {
        // given
        final String globalAssetId = "urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6";
        final IrsResponse expectedResponse = new IrsResponse(new Job("f41067c5-fad8-426c-903e-130ecac9c3da", globalAssetId,"COMPLETED"), new ArrayList<>(),
                List.of(exampleShellWithGlobalAssetId(globalAssetId)));

        // when
        final Optional<BpnData> requestorBPN = expectedResponse.findRequestorBPN();

        // then
        assertThat(requestorBPN).isNotEmpty();
        assertThat(requestorBPN.get().getId()).isEqualTo(globalAssetId);
        assertThat(requestorBPN.get().getBpn()).isNotBlank();
    }

    @Test
    public void shouldFindSupplierBpnValuesOfRequestor() {
        // given
        final String globalAssetId = "urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6";
        final String childId = "urn:uuid:4faa5c19-75da-4dd1-b93f-ce1729d371d6";
        final String childId2 = "urn:uuid:5d395f10-1b22-4394-a2c0-263ba9749ea4";
        final IrsResponse expectedResponse = new IrsResponse(new Job("f41067c5-fad8-426c-903e-130ecac9c3da", globalAssetId,"COMPLETED"),
                List.of(exampleRelationship(globalAssetId, childId), exampleRelationship(globalAssetId, childId2)),
                List.of(exampleShellWithGlobalAssetId(globalAssetId), exampleShellWithGlobalAssetId(childId), exampleShellWithGlobalAssetId(childId2)));

        // when
        final List<BpnData> suppliersBPN = expectedResponse.findSuppliersBPN(globalAssetId);

        // then
        assertThat(suppliersBPN).isNotEmpty();
        assertThat(suppliersBPN).hasSize(2);
    }

}
