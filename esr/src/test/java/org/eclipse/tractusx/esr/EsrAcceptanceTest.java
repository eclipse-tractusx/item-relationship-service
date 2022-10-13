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
        final String globalAssetId = "urn:uuid:993b2dd2-be30-4455-835d-1cd26bd1635c";
        final RequestSpecBuilder builder = new RequestSpecBuilder();
        builder.setBaseUri("https://irs-esr.dev.demo.catena-x.net");
        final RequestSpecification requestSpecification = builder.build();

        // Set wait strategy to 3 min for rest assured http client
        RestAssured.responseSpecification = new ResponseSpecBuilder()
                .expectResponseTime(Matchers.lessThan(180000L))
                .build();

        // when
        final EsrCertificateStatistics actualStatistics = given()
                                                             .spec(requestSpecification)
                                                             .when()
                                                             .get("/esr/esr-statistics/" + globalAssetId
                                                                     + "/asBuilt/ISO14001/submodel")
                                                             .then()
                                                             .statusCode(HttpStatus.OK.value())
                                                             .extract()
                                                             .as(EsrCertificateStatistics.class);
        final EsrCertificateStatistics.CertificateStatistics actualStateStatistics = actualStatistics.getCertificateStateStatistic();

        // then
        assertThat(actualStatistics).isNotNull();
        assertThat(actualStatistics.getCertificateName()).isNotNull();
        assertThat(actualStatistics.getCertificateName()).isEqualTo(CertificateType.ISO14001);
        assertThat(actualStateStatistics).isNotNull();
        assertThat(actualStateStatistics.getCertificatesWithStateValid()).isEqualTo(5);
        assertThat(actualStateStatistics.getCertificatesWithStateInvalid()).isEqualTo(3);
        assertThat(actualStateStatistics.getCertificatesWithStateUnknown()).isEqualTo(2);
        assertThat(actualStateStatistics.getCertificatesWithStateExceptional()).isEqualTo(31);
    }

}
