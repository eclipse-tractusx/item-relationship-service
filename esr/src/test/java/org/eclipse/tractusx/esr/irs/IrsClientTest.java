package org.eclipse.tractusx.esr.irs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tractusx.esr.irs.IrsFixture.exampleRelationship;
import static org.eclipse.tractusx.esr.irs.IrsFixture.exampleShell;
import static org.mockito.BDDMockito.given;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.tractusx.esr.irs.model.relationship.Relationship;
import org.eclipse.tractusx.esr.irs.model.relationship.LinkedItem;
import org.eclipse.tractusx.esr.irs.model.shell.Endpoint;
import org.eclipse.tractusx.esr.irs.model.shell.IdentifierKeyValuePair;
import org.eclipse.tractusx.esr.irs.model.shell.ListOfValues;
import org.eclipse.tractusx.esr.irs.model.shell.ProtocolInformation;
import org.eclipse.tractusx.esr.irs.model.shell.Shell;
import org.eclipse.tractusx.esr.irs.model.shell.SubmodelDescriptor;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

class IrsClientTest {

    private static final String URL = "https://irs-esr.dev.demo.catena-x.net/";
    private static final String TOKEN = "jwt-token";
    private static final String JOB_ID = "1545da82-e5b3-4fda-bc35-866dc6a29c4c";
    private final RestTemplate restTemplate = Mockito.mock(RestTemplate.class);

    @Test
    public void shouldStartJob() {
        // given
        IrsClient irsClient = new IrsClient(restTemplate, URL);
        IrsRequest irsRequest = IrsRequest.builder().globalAssetId("global-id").bomLifecycle("asBuilt").build();
        StartJobResponse expectedResponse = StartJobResponse.builder().jobId(JOB_ID).build();

        given(restTemplate.postForObject(URL + "/irs/jobs",
                new HttpEntity<>(irsRequest, withToken(TOKEN)), StartJobResponse.class))
                .willReturn(expectedResponse);

        // when
        final StartJobResponse actualResponse = irsClient.startJob(irsRequest, TOKEN);

        // then
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    public void shouldFetchResponseWithEmptyList() {
        // given
        IrsClient irsClient = new IrsClient(restTemplate, URL);
        IrsResponse expectedResponse = new IrsResponse(new Job("urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6","COMPLETED"), new ArrayList<>(),
                new ArrayList<>());

        given(restTemplate.exchange(getUriWithJobId(URL, JOB_ID), HttpMethod.GET,
                new HttpEntity<>(null, withToken(TOKEN)), IrsResponse.class)).willReturn(
                new ResponseEntity<>(expectedResponse, HttpStatus.OK));

        // when
        final IrsResponse actualResponse = irsClient.getJobDetails(JOB_ID, TOKEN);

        // then
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    public void shouldFetchRelationships() {
        // given
        IrsClient irsClient = new IrsClient(restTemplate, URL);
        Relationship irsRelationship = exampleRelationship();
        IrsResponse expectedResponse = new IrsResponse(new Job("urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6","COMPLETED"), List.of(irsRelationship),
                new ArrayList<>());

        given(restTemplate.exchange(getUriWithJobId(URL, JOB_ID), HttpMethod.GET,
                new HttpEntity<>(null, withToken(TOKEN)), IrsResponse.class)).willReturn(
                new ResponseEntity<>(expectedResponse, HttpStatus.OK));

        // when
        final IrsResponse actualResponse = irsClient.getJobDetails(JOB_ID, TOKEN);

        // then
        assertThat(actualResponse).isEqualTo(expectedResponse);
        assertThat(actualResponse.getRelationships()).hasSize(1);
        Relationship relationship = actualResponse.getRelationships().get(0);
        assertThat(relationship).usingRecursiveComparison().isEqualTo(irsRelationship);
    }

    @Test
    public void shouldFetchShell() {
        // given
        IrsClient irsClient = new IrsClient(restTemplate, URL);
        Shell expectedShell = exampleShell();
        IrsResponse expectedResponse = new IrsResponse(new Job("urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6","COMPLETED"), new ArrayList<>(),
                List.of(exampleShell()));

        given(restTemplate.exchange(getUriWithJobId(URL, JOB_ID), HttpMethod.GET,
                new HttpEntity<>(null, withToken(TOKEN)), IrsResponse.class)).willReturn(
                new ResponseEntity<>(expectedResponse, HttpStatus.OK));

        // when
        final IrsResponse actualResponse = irsClient.getJobDetails(JOB_ID, TOKEN);

        // then
        assertThat(actualResponse).isEqualTo(expectedResponse);
        assertThat(actualResponse.getShells()).hasSize(1);
        Shell actualShell = actualResponse.getShells().get(0);
        assertThat(actualShell).usingRecursiveComparison().isEqualTo(expectedShell);
    }

    private URI getUriWithJobId(String url, String jobId) {
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(url);
        uriBuilder.path("/irs/jobs/").path(jobId);
        return uriBuilder.build().toUri();
    }

    private HttpHeaders withToken(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.set("Authorization", "Bearer " + token);
        return headers;
    }



}