package org.eclipse.tractusx.esr.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tractusx.esr.irs.IrsFixture.exampleRelationship;
import static org.eclipse.tractusx.esr.irs.IrsFixture.exampleShellWithGlobalAssetId;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.tractusx.esr.controller.model.BomLifecycle;
import org.eclipse.tractusx.esr.controller.model.CertificateType;
import org.eclipse.tractusx.esr.controller.model.EsrCertificateStatistics;
import org.eclipse.tractusx.esr.irs.IrsFacade;
import org.eclipse.tractusx.esr.irs.IrsResponse;
import org.eclipse.tractusx.esr.irs.Job;
import org.eclipse.tractusx.esr.supplyon.CertificateState;
import org.eclipse.tractusx.esr.supplyon.EsrCertificate;
import org.eclipse.tractusx.esr.supplyon.SupplyOnFacade;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClientException;

class EsrServiceTest {

    private final IrsFacade irsFacade = mock(IrsFacade.class);
    private final SupplyOnFacade supplyOnFacade = mock(SupplyOnFacade.class);

    private final EsrService esrService = new EsrService(irsFacade, supplyOnFacade);

    @Test
    void shouldReturnEsrCertificateStatisticsWithIncrementedValidStateStatistic() {
        when(supplyOnFacade.getESRCertificate(anyString(), anyString())).thenReturn(EsrCertificate.builder().certificateState(
                CertificateState.VALID).build());

        final String globalAssetId = "urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6";
        final String childId = "urn:uuid:4faa5c19-75da-4dd1-b93f-ce1729d371d6";
        final IrsResponse irsResponse = IrsResponse.builder().job(Job.builder().jobId("f41067c5-fad8-426c-903e-130ecac9c3da").globalAssetId(globalAssetId).jobState("COMPLETED").build())
                .relationships(List.of(exampleRelationship(globalAssetId, childId)))
                .shells(List.of(exampleShellWithGlobalAssetId(globalAssetId), exampleShellWithGlobalAssetId(childId)))
                .build();
        when(irsFacade.getIrsResponse(eq(globalAssetId), anyString())).thenReturn(irsResponse);

        final EsrCertificateStatistics esrCertificateStatistics = esrService.handle(globalAssetId, BomLifecycle.AS_BUILT,
                CertificateType.ISO14001);

        assertThat(esrCertificateStatistics).isNotNull();
        assertThat(esrCertificateStatistics.getCertificateName()).isEqualTo(CertificateType.ISO14001);
        assertThat(esrCertificateStatistics.getCertificateStateStatistic()).isNotNull();
        assertThat(esrCertificateStatistics.getCertificateStateStatistic().getCertificatesWithStateValid()).isPositive();
    }

    @Test
    void shouldReturnEsrCertificateStatisticsWithIncrementedExceptionalStateStatisticWhenSupplyOnServiceResultsWithError() {
        when(supplyOnFacade.getESRCertificate(anyString(), anyString())).thenThrow(new RestClientException("SupplyOn Error"));

        final String globalAssetId = "urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6";
        final String childId = "urn:uuid:4faa5c19-75da-4dd1-b93f-ce1729d371d6";
        final IrsResponse irsResponse = IrsResponse.builder().job(Job.builder().jobId("f41067c5-fad8-426c-903e-130ecac9c3da").globalAssetId(globalAssetId).jobState("COMPLETED").build())
                                                   .relationships(List.of(exampleRelationship(globalAssetId, childId)))
                                                   .shells(List.of(exampleShellWithGlobalAssetId(globalAssetId), exampleShellWithGlobalAssetId(childId)))
                                                   .build();
        when(irsFacade.getIrsResponse(eq(globalAssetId), anyString())).thenReturn(irsResponse);

        final EsrCertificateStatistics esrCertificateStatistics = esrService.handle(globalAssetId, BomLifecycle.AS_BUILT,
                CertificateType.ISO14001);

        assertThat(esrCertificateStatistics).isNotNull();
        assertThat(esrCertificateStatistics.getCertificateName()).isEqualTo(CertificateType.ISO14001);
        assertThat(esrCertificateStatistics.getCertificateStateStatistic()).isNotNull();
        assertThat(esrCertificateStatistics.getCertificateStateStatistic().getCertificatesWithStateExceptional()).isPositive();
    }


}
