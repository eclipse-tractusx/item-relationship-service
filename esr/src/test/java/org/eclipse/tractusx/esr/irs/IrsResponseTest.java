package org.eclipse.tractusx.esr.irs;

import static org.assertj.core.api.Assertions.assertThat;
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
        final IrsResponse expectedResponse = new IrsResponse(new Job(globalAssetId,"COMPLETED"), new ArrayList<>(),
                List.of(exampleShellWithGlobalAssetId(globalAssetId)));

        // when
        final Optional<BpnData> requestorBPN = expectedResponse.findRequestorBPN();

        // then
        assertThat(requestorBPN).isNotEmpty();
        assertThat(requestorBPN.get().getId()).isEqualTo(globalAssetId);
        assertThat(requestorBPN.get().getBpn()).isNotBlank();
    }

}
