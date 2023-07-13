package org.eclipse.tractusx.ess.discovery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ConnectorEndpointsServiceTest {

    private final EssDiscoveryFinderClient essDiscoveryFinderClient = Mockito.mock(EssDiscoveryFinderClient.class);
    private final ConnectorEndpointsService service = new ConnectorEndpointsService(essDiscoveryFinderClient);

    @Test
    void shouldFindConnectorEndpoints() {
        // given
        final String bpn = "BPN123";
        given(essDiscoveryFinderClient.findDiscoveryEndpoints(any()))
                  .willReturn(new DiscoveryResponse(List.of(createEndpoint("address1"), createEndpoint("address2"))));
        given(essDiscoveryFinderClient.findConnectorEndpoints(eq("address1"), any()))
                .willReturn(List.of(createResult(List.of("connector1", "connector2"))));
        given(essDiscoveryFinderClient.findConnectorEndpoints(eq("address2"), any()))
                .willReturn(List.of(createResult(List.of("connector3", "connector4"))));

        // when
        final List<String> actualConnectors = service.fetchConnectorEndpoints(bpn);

        // then
        assertThat(actualConnectors).containsExactly("connector1", "connector2", "connector3", "connector4");
    }

    private DiscoveryEndpoint createEndpoint(final String endpointAddress) {
        return new DiscoveryEndpoint("test-endpoint", "desc", endpointAddress, "docs", "resId");
    }

    private EdcDiscoveryResult createResult(final List<String> connectors) {
        return new EdcDiscoveryResult("BPN123", connectors);
    }

}