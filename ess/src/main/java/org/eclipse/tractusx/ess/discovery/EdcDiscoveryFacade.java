package org.eclipse.tractusx.ess.discovery;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * Public API Facade for Discovery service domain
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EdcDiscoveryFacade {

    private static final String DEFAULT_EDC_ENDPOINT = "http://default";

    private final EdcDiscoveryClient edcDiscoveryClient;

    public String getEdcBaseUrl(final String bpn) {
        final EdcAddressResponse edcAddressResponse = edcDiscoveryClient.getEdcBaseUrl(bpn);

        final List<String> endpoints = edcAddressResponse.getConnectorEndpoint();

        return endpoints.stream()
                        .filter(StringUtils::isNotBlank)
                        .findFirst()
                        .orElse(DEFAULT_EDC_ENDPOINT);
    }

}
