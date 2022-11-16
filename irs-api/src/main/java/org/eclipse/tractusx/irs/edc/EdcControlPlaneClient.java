package org.eclipse.tractusx.irs.edc;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.dataspaceconnector.spi.types.domain.catalog.Catalog;
import org.eclipse.tractusx.irs.edc.model.NegotiationId;
import org.eclipse.tractusx.irs.edc.model.NegotiationRequest;
import org.eclipse.tractusx.irs.edc.model.NegotiationResponse;
import org.eclipse.tractusx.irs.edc.model.TransferProcessId;
import org.eclipse.tractusx.irs.edc.model.TransferProcessRequest;
import org.eclipse.tractusx.irs.edc.model.TransferProcessResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class EdcControlPlaneClient {

    static final String CONSUMER_CONTROL_PLANE = "https://irs-consumer-controlplane.dev.demo.catena-x.net";
//    static final String PROVIDER_CONTROL_PLANE = "https://irs-provider-controlplane3.dev.demo.catena-x.net";

    static final String CONTROL_PLANE_SUFIX = "/api/v1/ids/data";
    private static final String EDC_HEADER = "X-Api-Key";
    private static final String EDC_TOKEN = "";

    private final RestTemplate simpleRestTemplate;

    Catalog getCatalog(String providerConnectorUrl) {
        return simpleRestTemplate.exchange(CONSUMER_CONTROL_PLANE
                + "/data/catalog?providerUrl="
                + providerConnectorUrl
                + CONTROL_PLANE_SUFIX
                + "&limit=1000",
                HttpMethod.GET,
                new HttpEntity<>(null, headers()),
                Catalog.class).getBody();
    }

    NegotiationId startNegotiations(NegotiationRequest request) {
        return simpleRestTemplate.exchange(CONSUMER_CONTROL_PLANE
                        + "/data/contractnegotiations",
                HttpMethod.POST,
                new HttpEntity<>(request, headers()),
                NegotiationId.class).getBody();
    }

    @SneakyThrows
    NegotiationResponse getNegotiationResult(NegotiationId negotiationId) {

        NegotiationResponse response = null;

        int calls = 0;
        boolean confirmed = false;
        while (calls < 20 && !confirmed) {
            calls++;
            log.info("Check negotiations status for: {} time", calls);
            Thread.sleep(1000L);

            response = simpleRestTemplate.exchange(
                    CONSUMER_CONTROL_PLANE + "/data/contractnegotiations/" + negotiationId.getId(), HttpMethod.GET,
                    new HttpEntity<>(null, headers()), NegotiationResponse.class).getBody();

            log.info("Response status of negotiation: {}", response);

            if (response != null) {
                confirmed = "CONFIRMED".equals(response.getState());
            }

        }
        return response;
    }

    TransferProcessId startTransferProcess(TransferProcessRequest request) {
        return simpleRestTemplate.exchange(CONSUMER_CONTROL_PLANE
                        + "/data/transferprocess",
                HttpMethod.POST,
                new HttpEntity<>(request, headers()),
                TransferProcessId.class).getBody();
    }

    @SneakyThrows
    TransferProcessResponse getTransferProcess(TransferProcessId transferProcessId) {
        TransferProcessResponse response = null;

        int calls = 0;
        boolean completed = false;
        while (calls < 20 && !completed) {
            calls++;
            log.info("Check Transfer Process status for: {} time", calls);
            Thread.sleep(2000L);

            response = simpleRestTemplate.exchange(
                    CONSUMER_CONTROL_PLANE + "/data/transferprocess/" + transferProcessId.getId(),
                    HttpMethod.GET,
                    new HttpEntity<>(null, headers()),
                    TransferProcessResponse.class).getBody();

            log.info("Response status of Transfer Process: {}", response);

            if (response != null) {
                completed = "COMPLETED".equals(response.getState());
            }

        }
        return response;
    }

    private HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.add(EDC_HEADER, EDC_TOKEN);
        return headers;
    }

}
