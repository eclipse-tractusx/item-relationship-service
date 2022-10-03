package org.eclipse.tractusx.esr;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.eclipse.tractusx.esr.controller.model.CertificateType;
import org.eclipse.tractusx.esr.controller.model.EsrCertificateStatistics;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class EsrAcceptanceTest {

    @Test
    void shouldRetrieveEsrCertificateStatisticsWithExpectedStates() {
        // given
        final String globalAssetId = "urn:uuid:2ab59090-5406-444a-87c1-3d0b6bc85718";
        final RequestSpecBuilder builder = new RequestSpecBuilder();
        builder.setBaseUri("https://irs-esr.dev.demo.catena-x.net");
        final RequestSpecification requestSpecification = builder.build();

        // Set wait strategy to 3 min for rest assured http client
        RestAssured.responseSpecification = new ResponseSpecBuilder()
                .expectResponseTime(Matchers.lessThan(180000L))
                .build();

        // when
        final EsrCertificateStatistics esrCertificateStatistics = given()
                                                             .spec(requestSpecification)
                                                             .when()
                                                             .get("/esr/esr-statistics/" + globalAssetId
                                                                     + "/asBuilt/ISO14001/submodel")
                                                             .then()
                                                             .statusCode(HttpStatus.OK.value())
                                                             .extract()
                                                             .as(EsrCertificateStatistics.class);

        // then
        assertThat(esrCertificateStatistics).isNotNull();
        assertThat(esrCertificateStatistics.getCertificateName()).isNotNull();
        assertThat(esrCertificateStatistics.getCertificateName()).isEqualTo(CertificateType.ISO14001);
        assertThat(esrCertificateStatistics.getCertificateStateStatistic()).isNotNull();
        assertThat(esrCertificateStatistics.getCertificateStateStatistic().getCertificatesWithStateValid()).isEqualTo(21);
        assertThat(esrCertificateStatistics.getCertificateStateStatistic().getCertificatesWithStateInvalid()).isEqualTo(3);
        assertThat(esrCertificateStatistics.getCertificateStateStatistic().getCertificatesWithStateUnknown()).isEqualTo(2);
        assertThat(esrCertificateStatistics.getCertificateStateStatistic().getCertificatesWithStateExceptional()).isEqualTo(1);
    }

}
