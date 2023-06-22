package org.eclipse.tractusx.irs.aaswrapper.registry.domain;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Connector Endpoints service to find connectors in Discovery Finder
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConnectorEndpointsService {

    private final DiscoveryFinderClient discoveryFinderClient;

    public List<String> fetchConnectorEndpoints(final String bpn) {
        final DiscoveryFinderRequest onlyBpn = new DiscoveryFinderRequest(List.of("bpn"));
        final List<DiscoveryEndpoint> discoveryEndpoints = discoveryFinderClient.findDiscoveryEndpoints(onlyBpn)
                                                                                .endpoints();
        final List<String> providedBpn = List.of(bpn);
        return discoveryEndpoints.stream()
                                 .map(discoveryEndpoint -> discoveryFinderClient.findConnectorEndpoints(
                                                                                        discoveryEndpoint.endpointAddress(),
                                                                                        providedBpn)
                                                                                .stream()
                                                                                .filter(edcDiscoveryResult -> edcDiscoveryResult.bpn()
                                                                                                                                .equals(bpn))
                                                                                .map(EdcDiscoveryResult::connectorEndpoint)
                                                                                .toList())
                                 .flatMap(List::stream)
                                 .flatMap(List::stream)
                                 .toList();
    }

}
