//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.smoketest;

import net.catenax.irs.component.JobHandle;
import net.catenax.irs.component.Jobs;
import net.catenax.irs.component.RegisterJob;
import net.catenax.irs.component.enums.JobState;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke tests verify that the cloud infrastructure where IRS runs is working as expected
 * @see <a href="https://confluence.catena-x.net/display/ARTI/MTPDC+Testing">MTPDC Testing</a>
 */
@Tag("SmokeTests")
class IrsApiSmokeTests extends SmokeTestsBase {

    @Test
    void shouldCreateAndCompleteJob() {

        var responsePost =
            given()
                .spec(getRequestSpecification())
                .baseUri(irsApiUri)
                .body(new RegisterJob())
            .when()
                .post(PATH_CREATE_JOB)
            .then()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .extract().as(JobHandle.class);

        assertThat(responsePost.getJobId()).isNotNull();

        var responseGet =
            given()
                .spec(getRequestSpecification())
                .baseUri(irsApiUri)
                .pathParam("jobId", responsePost.getJobId())
            .when()
                .get(PATH_GET_JOB)
            .then()
                .assertThat()
                .statusCode(HttpStatus.OK.value())
                .extract().as(Jobs.class);

        assertThat(responseGet.getJob().getJobState()).isEqualTo(JobState.COMPLETED);
    }

}
