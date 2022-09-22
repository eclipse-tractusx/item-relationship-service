package org.eclipse.tractusx.esr.controller.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EsrCertificateStatisticsTest {

    @Test
    public void shouldIncrementEachStatistics() {
        // given
        EsrCertificateStatistics given = EsrCertificateStatistics.initial()
                    .incrementBy(EsrCertificateStatistics.builder().certificateStateStatistic(
                        EsrCertificateStatistics.CertificateStatistics.builder()
                            .certificatesWithStateValid(3)
                            .certificatesWithStateInvalid(5)
                            .certificatesWithStateUnknown(2)
                            .certificatesWithStateExceptional(4)
                            .build())
                        .build());

        // when
        EsrCertificateStatistics increment = given
                    .incrementBy(EsrCertificateStatistics.builder()
                        .certificateStateStatistic(
                            EsrCertificateStatistics.CertificateStatistics.builder()
                                .certificatesWithStateValid(40)
                                .certificatesWithStateInvalid(30)
                                .certificatesWithStateUnknown(20)
                                .certificatesWithStateExceptional(10)
                                .build())
                            .build());

        // then
        final EsrCertificateStatistics.CertificateStatistics actual = increment.getCertificateStateStatistic();
        assertThat(actual.getCertificatesWithStateValid()).isEqualTo(43);
        assertThat(actual.getCertificatesWithStateInvalid()).isEqualTo(35);
        assertThat(actual.getCertificatesWithStateUnknown()).isEqualTo(22);
        assertThat(actual.getCertificatesWithStateExceptional()).isEqualTo(14);

    }

}