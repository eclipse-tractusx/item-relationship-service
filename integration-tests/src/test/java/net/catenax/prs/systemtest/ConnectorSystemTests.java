//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.prs.systemtest;

import net.catenax.prs.requests.PartsTreeByObjectIdRequest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static io.restassured.RestAssured.given;
import static java.lang.String.format;
import static net.catenax.prs.systemtest.SystemTestsBase.ASPECT_MATERIAL;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;


/**
 * System tests that verify the interaction between Consumer and Provider connectors.
 * <p>
 * The current implementation expects the Provider to be a singleton pod.
 *
 * @see <a href="https://confluence.catena-x.net/display/ARTI/MTPDC+Testing">MTPDC Testing</a>
 */
@Tag("SystemTests")
public class ConnectorSystemTests {

    private static final String consumerURI = System.getProperty("ConnectorConsumerURI",
            "https://catenaxdev001akssrv.germanywestcentral.cloudapp.azure.com/prs-connector-consumer");
    private static final String VEHICLE_ONEID = "CAXSWPFTJQEVZNZZ";
    private static final String VEHICLE_OBJECTID = "UVVZI9PKX5D37RFUB";

    @Test
    public void downloadFile() throws Exception {

        // Arrange
        var environment = System.getProperty("environment", "dev");

        var fileWithExpectedOutput = format("getPartsTreeByOneIdAndObjectId-%s-expected.json", environment);
        InputStream resourceAsStream = getClass().getResourceAsStream(fileWithExpectedOutput);
        Objects.requireNonNull(resourceAsStream);
        var expectedResult = new String(resourceAsStream.readAllBytes());

        // Act

        // Send query to Consumer connector, to perform file copy on Provider
        Map<String, Object> params = new HashMap<>();
        params.put("byObjectIdRequest", PartsTreeByObjectIdRequest.builder()
                .oneIDManufacturer(VEHICLE_ONEID)
                .objectIDManufacturer(VEHICLE_OBJECTID)
                .view("AS_BUILT")
                .aspect(ASPECT_MATERIAL)
                .depth(2)
                .build());

        var requestId =
                given()
                        .baseUri(consumerURI)
                        .contentType("application/json")
                        .body(params)
                .when()
                        .post("/api/v0.1/retrievePartsTree")
                .then()

                        .assertThat()
                        .statusCode(HttpStatus.OK.value())
                        .extract().asString();

        // An ID is returned, for polling
        assertThat(requestId).isNotBlank();

        // Get sasUrl
        await()
                .atMost(Duration.ofSeconds(120))
                .pollInterval(Duration.ofSeconds(1))
                .untilAsserted(() -> getSasUrl(requestId));

        // retrieve blob
        var sasUrl = getSasUrl(requestId);

        // Assert
        String result = getUrl(sasUrl);

        assertThatJson(result)
                .when(IGNORING_ARRAY_ORDER)
                .isEqualTo(expectedResult);
    }

    private String getUrl(String sasUrl) throws IOException, InterruptedException {
        var httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        var request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(sasUrl))
                .build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(200);

        return response.body();
    }

    private String getSasUrl(String requestId) {
        return
                given()
                        .baseUri(consumerURI)
                        .pathParam("requestId", requestId)
                .when()
                        .get("/api/v0.1/datarequest/{requestId}/state")
                .then()
                        .assertThat()
                        .statusCode(HttpStatus.OK.value())
                        .extract().asString();
    }
}
