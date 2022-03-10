package net.catenax.prs.smoketest;

import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import net.catenax.prs.dtos.PartRelationshipsWithInfos;
import net.catenax.prs.dtos.events.PartRelationshipsUpdateRequest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static io.restassured.RestAssured.given;
import static net.catenax.prs.dtos.PartsTreeView.AS_BUILT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * E2E Smoke tests verify that the cloud infrastructure where both PRS and broker-proxy run is working as expected
 * @see <a href="https://confluence.catena-x.net/display/ARTI/MTPDC+Testing">MTPDC Testing</a>
 */
@Tag("SmokeTests")
public class End2EndSmokeTests extends SmokeTestsBase {

    @Test
    public void updateRelationshipsAndGetPartsTree_success() {

        RequestSpecification specification = getRequestSpecification();

        var partRelationshipUpdate = generate.partRelationshipUpdate();
        var updateRequest = PartRelationshipsUpdateRequest.builder().withRelationships(List.of(partRelationshipUpdate)).build();
        var partRelationship = partRelationshipUpdate.getRelationship();
        var parent = partRelationship.getParent();

        given()
            .spec(specification)
            .baseUri(brokerProxyUri)
            .contentType(ContentType.JSON)
            .body(updateRequest)
        .when()
            .post(PATH_UPDATE_RELATIONSHIPS)
        .then()
            .assertThat()
            .statusCode(HttpStatus.NO_CONTENT.value());

        await().untilAsserted(() -> {
            var response =
                    given()
                        .spec(specification)
                        .baseUri(prsApiUri)
                        .pathParam(ONE_ID_MANUFACTURER, parent.getOneIDManufacturer())
                        .pathParam(OBJECT_ID_MANUFACTURER, parent.getObjectIDManufacturer())
                        .queryParam(VIEW, AS_BUILT)
                    .when()
                        .get(PATH_BY_IDS)
                        .then()
                        .assertThat()
                        .statusCode(HttpStatus.OK.value())
                        .extract().as(PartRelationshipsWithInfos.class);

            assertThat(response.getRelationships()).containsExactly(partRelationship);
        });

    }
}
