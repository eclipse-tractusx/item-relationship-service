package org.eclipse.tractusx.esr.irs;

import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class IrsClient {

    private final static String IRS_URL = "https://irs.dev.demo.catena-x.net";
    private final RestTemplate restTemplate;

    IrsClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    StartJobResponse startJob(IrsRequest irsRequest, String authorizationToken) {
        return restTemplate.postForObject(IRS_URL + "/irs/jobs",
                new HttpEntity<>(irsRequest, tokenInHeaders(authorizationToken)), StartJobResponse.class);
    }

    @Retry(name = "waiting-for-completed")
    IrsResponse getJobDetails(String jobId, String authorizationToken) {
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(IRS_URL);
        uriBuilder.path("/irs/jobs/").path(jobId);
        return restTemplate.exchange(
                uriBuilder.build().toUri(),
                HttpMethod.GET,
                new HttpEntity<>("parameters", tokenInHeaders(authorizationToken)),
                IrsResponse.class).getBody();
    }

    private static HttpHeaders tokenInHeaders(final String authorizationToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.set("Authorization", authorizationToken);
        return headers;
    }

}
