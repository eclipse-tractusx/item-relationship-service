package org.eclipse.tractusx.esr.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tractusx.esr.irs.IrsFixture.exampleShellWithGlobalAssetId;
import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.tractusx.esr.controller.model.EsrCertificateStatistics;
import org.eclipse.tractusx.esr.irs.IrsResponse;
import org.eclipse.tractusx.esr.irs.Job;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

class EsrCertificateAggregationTest {

    private final RestTemplate restTemplate = Mockito.mock(RestTemplate.class);

    @Test
    public void shouldAggregateStatistics() {
        // given
        final EsrCertificateAggregation esrCertificateAggregation = new EsrCertificateAggregation(restTemplate);

        final String globalAssetId = "urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6";

        final IrsResponse irsResponse = IrsResponse.builder()
                .job(Job.builder()
                        .jobId("f41067c5-fad8-426c-903e-130ecac9c3da")
                        .globalAssetId(globalAssetId)
                        .jobState("COMPLETED").build())
                .shells(List.of(exampleShellWithGlobalAssetId(globalAssetId, "urn:bamm:io.catenax.esr_certificates.esr_certificate:1.0.0")))
                .relationships(new ArrayList<>())
                .build();

        final EsrCertificateStatistics subStatistics = EsrCertificateStatistics.builder()
                .certificateStateStatistic(EsrCertificateStatistics.CertificateStatistics.builder()
                        .certificatesWithStateValid(8)
                        .certificatesWithStateInvalid(2)
                        .build())
                .build();

        given(restTemplate.getForEntity("urn:uuid:4ad4a1ce-beb2-42d2-bfe7-d5d9c68d6daf", EsrCertificateStatistics.class))
                .willReturn(new ResponseEntity<>(subStatistics, HttpStatus.OK));

        // when
        final EsrCertificateStatistics actual = esrCertificateAggregation.aggregateStatistics(irsResponse, EsrCertificateStatistics.initial());

        // then
        assertThat(actual.getCertificateStateStatistic().getCertificatesWithStateValid()).isEqualTo(8);
        assertThat(actual.getCertificateStateStatistic().getCertificatesWithStateInvalid()).isEqualTo(2);
    }

    @Test
    public void shouldDoNothingWhenEsrDoNotResponseWithStatistics() {
        // given
        final EsrCertificateAggregation esrCertificateAggregation = new EsrCertificateAggregation(restTemplate);

        final String globalAssetId = "urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6";

        final IrsResponse irsResponse = IrsResponse.builder()
                                                   .job(Job.builder()
                                                           .jobId("f41067c5-fad8-426c-903e-130ecac9c3da")
                                                           .globalAssetId(globalAssetId)
                                                           .jobState("COMPLETED").build())
                                                   .shells(List.of(exampleShellWithGlobalAssetId(globalAssetId, "urn:bamm:io.catenax.esr_certificates.esr_certificate:1.0.0")))
                                                   .relationships(new ArrayList<>())
                                                   .build();


        given(restTemplate.getForEntity("urn:uuid:4ad4a1ce-beb2-42d2-bfe7-d5d9c68d6daf", EsrCertificateStatistics.class))
                .willReturn(new ResponseEntity<>(null, HttpStatus.BAD_GATEWAY));

        // when
        final EsrCertificateStatistics actual = esrCertificateAggregation.aggregateStatistics(irsResponse, EsrCertificateStatistics.initial());

        // then
        assertThat(actual.getCertificateStateStatistic().getCertificatesWithStateValid()).isEqualTo(0);
        assertThat(actual.getCertificateStateStatistic().getCertificatesWithStateInvalid()).isEqualTo(0);
    }

}