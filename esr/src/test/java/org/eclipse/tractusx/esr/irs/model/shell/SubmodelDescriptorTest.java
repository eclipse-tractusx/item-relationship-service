package org.eclipse.tractusx.esr.irs.model.shell;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class SubmodelDescriptorTest {

    @Test
    public void shouldBeConsiderAsRelationship() {
        // given
        SubmodelDescriptor descriptor = SubmodelDescriptor.builder()
                                              .semanticId(ListOfValues.builder()
                                                  .value(List.of("urn:bamm:io.catenax.assembly_part_relationship:1.0.0"))
                                                  .build())
                                              .build();

        // when
        Boolean actual = descriptor.isPartRelationship();

        // then
        assertThat(actual).isTrue();
    }

    @Test
    public void shouldNotBeConsiderAsRelationship() {
        // given
        SubmodelDescriptor descriptor = SubmodelDescriptor.builder()
                                              .semanticId(ListOfValues.builder()
                                                  .value(List.of("urn:bamm:io.catenax.any_other_descriptor:1.0.0"))
                                                  .build())
                                              .build();

        // when
        Boolean actual = descriptor.isPartRelationship();

        // then
        assertThat(actual).isFalse();
    }

    @Test
    public void shouldBeConsiderAsEsrCertificate() {
        // given
        SubmodelDescriptor descriptor = SubmodelDescriptor.builder()
                                              .semanticId(ListOfValues.builder()
                                                  .value(List.of("urn:bamm:io.catenax.esr_certificates.esr_certificate_state_statistic:1.0.1#EsrCertificateStateStatistic"))
                                                  .build())
                                              .build();

        // when
        Boolean actual = descriptor.isEsrCertificate();

        // then
        assertThat(actual).isTrue();
    }

    @Test
    public void shouldNotBeConsiderAsEsrCertificate() {
        // given
        SubmodelDescriptor descriptor = SubmodelDescriptor.builder()
                                              .semanticId(ListOfValues.builder()
                                                  .value(List.of("urn:bamm:io.catenax.any_other_descriptor:1.0.0"))
                                                  .build())
                                              .build();

        // when
        Boolean actual = descriptor.isEsrCertificate();

        // then
        assertThat(actual).isFalse();
    }

}