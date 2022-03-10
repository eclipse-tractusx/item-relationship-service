package net.catenax.brokerProxy;

import io.restassured.http.ContentType;
import net.catenax.prs.dtos.Aspect;
import net.catenax.prs.dtos.events.PartAspectsUpdateRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static net.catenax.prs.dtos.ValidationConstants.ASPECT_UPDATE_LIST_MAX_SIZE;
import static net.catenax.prs.dtos.ValidationConstants.INPUT_FIELD_MAX_LENGTH;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static org.assertj.core.api.Assertions.assertThat;

public class UpdatePartAspectTest extends BrokerProxyIntegrationTestBase {

    private static final String PATH = "/broker-proxy/v0.1/partAspectUpdate";

    @Test
    public void updatedPartAspectUpdate_success() throws Exception {

        var event = generate.partAspectUpdate();

        given()
            .contentType(ContentType.JSON)
            .body(event)
        .when()
            .post(PATH)
        .then()
            .assertThat()
            .statusCode(HttpStatus.NO_CONTENT.value());

        assertThat(hasExpectedBrokerEvent(event, PartAspectsUpdateRequest.class)).isTrue();

    }

    @Test
    public void updatedPartAspectUpdateBadRequest_failure() {

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
    @MethodSource("provideInvalidAspects")
    public void updatedPartAspectUpdateWithInvalidAspects_failure(String name, List<Aspect> aspects, List<String> expectedErrors) {

        var response =
            given()
                .contentType(ContentType.JSON)
                .body(generate.partAspectUpdate().toBuilder().withAspects(aspects).build())
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
    public void updatedPartAspectUpdateWithNoPartId_failure() {

        var response =
            given()
                .contentType(ContentType.JSON)
                .body(generate.partAspectUpdate().toBuilder().withPart(null).build())
            .when()
                .post(PATH)
            .then()
                .assertThat()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().asString();

        assertThatJson(response)
                .isEqualTo(generateResponse.invalidArgument(List.of("part:must not be null")));

    }

    @ParameterizedTest(name = "{index} {1}")
    @MethodSource("provideInvalidEffectTime")
    public void updatedPartAspectUpdateWithInvalidEffectTime_failure(Instant effectTime, String expectedError) {

        var response =
            given()
                .contentType(ContentType.JSON)
                .body(generate.partAspectUpdate().toBuilder().withEffectTime(effectTime).build())
            .when()
                .post(PATH)
            .then()
                .assertThat()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract().asString();

        assertThatJson(response)
                .isEqualTo(generateResponse.invalidArgument(List.of(expectedError)));
    }

    /**
     * Provides invalid aspects test data.
     * @return Invalid aspects as {@link Stream} of {@link Arguments}.
     */
    private static Stream<Arguments> provideInvalidAspects() {
        return Stream.of(
                Arguments.of("Null aspect", null, List.of("aspects:Aspects list can't be empty. Use remove field to remove part aspects.")),
                Arguments.of("Empty aspect", Collections.emptyList(), List.of("aspects:Aspects list can't be empty. Use remove field to remove part aspects.", "aspects:size must be between 1 and 1000")),
                Arguments.of("Too many aspects", IntStream.rangeClosed(0, ASPECT_UPDATE_LIST_MAX_SIZE).mapToObj(i -> generateDto.partAspect()).collect(Collectors.toList()), List.of("aspects:size must be between 1 and 1000")),
                Arguments.of("Aspect with null name and url", List.of(generateDto.partAspect().toBuilder().withName(null).withUrl(null).build()), List.of("aspects[0].name:must not be blank", "aspects[0].url:must not be blank")),
                Arguments.of("Aspect with empty name and url", List.of(generateDto.partAspect().toBuilder().withName(EMPTY).withUrl(EMPTY).build()), List.of("aspects[0].name:size must be between 1 and 10000", "aspects[0].name:must not be blank", "aspects[0].url:size must be between 1 and 10000", "aspects[0].url:must not be blank")),
                Arguments.of("Aspect with name with only whitespace", List.of(generateDto.partAspect().toBuilder().withName(faker.regexify(WHITESPACE_REGEX)).build()), List.of("aspects[0].name:must not be blank")),
                Arguments.of("Aspect with invalid url", List.of(generateDto.partAspect().toBuilder().withUrl(faker.lorem().word()).build()), List.of("aspects[0].url:must be a valid URL")),
                Arguments.of("Aspect with too long name and url", List.of(generateDto.partAspect().toBuilder().withName(faker.lorem().characters(INPUT_FIELD_MAX_LENGTH + 1)).withUrl("https://" + faker.lorem().characters(INPUT_FIELD_MAX_LENGTH + 1) + "/aspect").build()), List.of("aspects[0].name:size must be between 1 and 10000", "aspects[0].url:size must be between 1 and 10000"))
        );
    }
}
