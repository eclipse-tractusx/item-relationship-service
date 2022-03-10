//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.brokerProxy;

import io.restassured.http.ContentType;
import net.catenax.prs.dtos.events.PartAttributeUpdateRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static net.catenax.prs.dtos.ValidationConstants.INPUT_FIELD_MAX_LENGTH;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.assertj.core.api.Assertions.assertThat;

public class UpdatePartsAttributesTest extends BrokerProxyIntegrationTestBase {

    private static final String PATH = "/broker-proxy/v0.1/partAttributeUpdate";

    @Test
    public void updatedPartsAttributes_success() throws Exception {

        var event = generate.partAttributeUpdate();

        given()
            .contentType(ContentType.JSON)
            .body(event)
        .when()
            .post(PATH)
        .then()
            .assertThat()
            .statusCode(HttpStatus.NO_CONTENT.value());

        assertThat(hasExpectedBrokerEvent(event, PartAttributeUpdateRequest.class)).isTrue();
    }

    @Test
    public void updatedPartsAttributesBadRequest_failure() {

        given()
            .contentType(ContentType.JSON)
            .body("bad request")
        .when()
            .post(PATH)
        .then()
            .assertThat()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("provideInvalidAttributeNames")
    public void updatedPartsAttributesInvalidAttributeName_failure(String name, String value, List<String> expectedErrors) {

        var response =
                given()
                    .contentType(ContentType.JSON)
                    .body(generate.partAttributeUpdate().toBuilder().withName(value).build())
                .when()
                    .post(PATH)
                .then()
                    .assertThat()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .extract().asString();

        assertThatJson(response)
                .when(IGNORING_ARRAY_ORDER)
                .isEqualTo(generateResponse.invalidArgument(expectedErrors));
    }

    @ParameterizedTest(name = "{index} {1}")
    @MethodSource("provideInvalidEffectTime")
    public void updatedPartsAttributesInvalidEffectTime_failure(Instant effectTime, String expectedError) {

        var response =
                given()
                    .contentType(ContentType.JSON)
                    .body(generate.partAttributeUpdate().toBuilder().withEffectTime(effectTime).build())
                .when()
                    .post(PATH)
                .then()
                    .assertThat()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .extract().asString();

        assertThatJson(response)
                .isEqualTo(generateResponse.invalidArgument(List.of(expectedError)));
    }

    @ParameterizedTest(name = "{index} {0}")
    @MethodSource("provideInvalidAttributeValues")
    public void updatedPartsAttributesInvalidAttrValue_failure(String name, String value, List<String> expectedErrors) {

        var response =
                given()
                    .contentType(ContentType.JSON)
                    .body(generate.partAttributeUpdate().toBuilder().withValue(value).build())
                .when()
                    .post(PATH)
                .then()
                    .assertThat()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .extract().asString();

        assertThatJson(response)
                .when(IGNORING_ARRAY_ORDER)
                .isEqualTo(generateResponse.invalidArgument(expectedErrors));
    }

    @Test
    public void updatedPartsAttributesNoPartId_failure() {

        var response =
                given()
                    .contentType(ContentType.JSON)
                    .body(generate.partAttributeUpdate().toBuilder().withPart(null).build())
                .when()
                    .post(PATH)
                .then()
                    .assertThat()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .extract().asString();

        assertThatJson(response)
                .isEqualTo(generateResponse.invalidArgument(List.of("part:must not be null")));
    }

    /**
     * Provides invalid attribute name test data.
     * @return Invalid attribute names as {@link Stream} of {@link Arguments}.
     */
    private static Stream<Arguments> provideInvalidAttributeNames() {
        return Stream.of(
                Arguments.of("Null value", null, List.of("name:must not be blank")),
                Arguments.of("Empty value", EMPTY, List.of("name:Invalid attribute name.", "name:must not be blank")),
                Arguments.of("Only whitespace", faker.regexify(WHITESPACE_REGEX), List.of("name:Invalid attribute name.", "name:must not be blank")),
                Arguments.of("Invalid name", faker.lorem().word(), List.of("name:Invalid attribute name."))
        );
    }

    /**
     * Provides invalid attribute value test data.
     * @return Invalid attribute values as {@link Stream} of {@link Arguments}.
     */
    private static Stream<Arguments> provideInvalidAttributeValues() {
        return Stream.of(
                Arguments.of("Null value", null, List.of("value:must not be blank")),
                Arguments.of("Empty value", EMPTY, List.of("value:must not be blank", "value:size must be between 1 and 10000")),
                Arguments.of("Only whitespace", faker.regexify(WHITESPACE_REGEX), List.of("value:must not be blank")),
                Arguments.of("Too long attribute value", faker.lorem().characters(INPUT_FIELD_MAX_LENGTH + 1), List.of("value:size must be between 1 and 10000"))
        );
    }
}
