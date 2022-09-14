package org.eclipse.tractusx.esr.irs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.math.BigDecimal;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.tractusx.esr.irs.model.relationship.IrsRelationship;
import org.eclipse.tractusx.esr.irs.model.relationship.LinkedItem;
import org.eclipse.tractusx.esr.irs.model.relationship.MeasurementUnit;
import org.eclipse.tractusx.esr.irs.model.relationship.Quantity;
import org.eclipse.tractusx.esr.irs.model.shell.AdministrativeInformation;
import org.eclipse.tractusx.esr.irs.model.shell.Description;
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
        IrsResponse expectedResponse = new IrsResponse(new JobStatus("COMPLETED"), new ArrayList<>(),
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
        IrsRelationship irsRelationship = exampleRelationship();
        IrsResponse expectedResponse = new IrsResponse(new JobStatus("COMPLETED"), List.of(irsRelationship),
                new ArrayList<>());

        given(restTemplate.exchange(getUriWithJobId(URL, JOB_ID), HttpMethod.GET,
                new HttpEntity<>(null, withToken(TOKEN)), IrsResponse.class)).willReturn(
                new ResponseEntity<>(expectedResponse, HttpStatus.OK));

        // when
        final IrsResponse actualResponse = irsClient.getJobDetails(JOB_ID, TOKEN);

        // then
        assertThat(actualResponse).isEqualTo(expectedResponse);
        assertThat(actualResponse.getRelationships()).hasSize(1);
        IrsRelationship relationship = actualResponse.getRelationships().get(0);
        assertThat(relationship).usingRecursiveComparison().isEqualTo(irsRelationship);
    }

    @Test
    public void shouldFetchShell() {
        // given
        IrsClient irsClient = new IrsClient(restTemplate, URL);
        Shell expectedShell = exampleShell();
        IrsResponse expectedResponse = new IrsResponse(new JobStatus("COMPLETED"), new ArrayList<>(),
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

    private IrsRelationship exampleRelationship() {
        return IrsRelationship.builder()
                    .catenaXId("urn:uuid:d9bec1c6-e47c-4d18-ba41-0a5fe8b7f447")
                    .aspectType("aspectType")
                    .linkedItem(LinkedItem.builder()
                            .assembledOn(ZonedDateTime.now())
                            .childCatenaXId("urn:uuid:a45a2246-f6e1-42da-b47d-5c3b58ed62e9")
                            .lastModifiedOn(ZonedDateTime.now())
                            .lifecycleContext("asBuilt")
                            .quantity(Quantity.builder()
                                    .measurementUnit(MeasurementUnit.builder()
                                            .datatypeURI(
                                                "urn:bamm:io.openmanufacturing:meta-model:1.0.0#piece")
                                            .lexicalValue("piece")
                                            .build())
                                    .quantityNumber(BigDecimal.TEN)
                            .build())
                    .build())
                .build();
    }

    private Shell exampleShell() {
        return Shell.builder()
                .administration(AdministrativeInformation.builder()
                        .revision("example-revision")
                        .version("example-version")
                        .build())
                .descriptions(List.of(Description.builder()
                        .language("example-language")
                        .text("example-text")
                        .build()))
                .globalAssetIds(ListOfValues.builder()
                        .value(List.of("urn:uuid:4ad4a1ce-beb2-42d2-bfe7-d5d9c68d6daf"))
                        .build())
                .idShort("idShort")
                .identification("urn:uuid:4ad4a1ce-beb2-42d2-bfe7-d5d9c68d6daf")
                .specificAssetIds(List.of(IdentifierKeyValuePair.builder()
                        .key("ManufacturerId")
                        .subjectId(ListOfValues.builder().value(List.of("sub-id")).build())
                        .value("BPNL00000003AYRE")
                        .semanticId(ListOfValues.builder().value(List.of("urn:bamm:com.catenax.serial_part_typization:1.0.0")).build())
                        .build()))
                .submodelDescriptors(List.of(SubmodelDescriptor.builder()
                        .administration(AdministrativeInformation.builder()
                                .revision("example-revision")
                                .version("example-version")
                                .build())
                        .idShort("idShort")
                        .identification("urn:uuid:4ad4a1ce-beb2-42d2-bfe7-d5d9c68d6daf")
                        .semanticId(ListOfValues.builder().value(List.of("urn:bamm:com.catenax.serial_part_typization:1.0.0")).build())
                                .endpoints(List.of(Endpoint.builder()
                                        .protocolInformation(ProtocolInformation.builder()
                                                .endpointAddress("urn:uuid:4ad4a1ce-beb2-42d2-bfe7-d5d9c68d6daf")
                                                .endpointProtocol("AAS/SUBMODEL")
                                                .endpointProtocolVersion("1.0RC02")
                                                .subprotocol("sub protocol")
                                                .subprotocolBody("sub protocol body")
                                                .subprotocolBodyEncoding("UTF-8")
                                                .build())
                                        .interfaceInfo("https://TEST.connector")
                                .build()))
                        .build()))
                .build();
    }

}