//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.prs.integrationtest;

import com.github.javafaker.Faker;
import net.catenax.prs.dtos.PartLifecycleStage;
import net.catenax.prs.dtos.PartRelationshipsWithInfos;
import net.catenax.prs.dtos.events.PartRelationshipUpdate;
import net.catenax.prs.testing.DtoMother;
import net.catenax.prs.testing.UpdateRequestMother;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static java.util.concurrent.TimeUnit.SECONDS;
import static net.catenax.prs.dtos.PartsTreeView.AS_BUILT;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class PrsRelationshipsUpdateProcessorTests extends PrsIntegrationTestsBase {

    private final static UpdateRequestMother generate = new UpdateRequestMother();
    private final static DtoMother generateDto = new DtoMother();

    @Test
    public void updatePartsRelationship_success() throws Exception {

        //Arrange
        var relationshipUpdate = generateAddedRelationship();
        var event = generate.partRelationshipUpdateList(relationshipUpdate);
        var relationship = relationshipUpdate.getRelationship();
        var parent = relationship.getParent();

        //Act
        publishUpdateEvent(event);

        //Assert
        await().untilAsserted(() -> {
            var response =
                    given()
                        .pathParam(ONE_ID_MANUFACTURER, parent.getOneIDManufacturer())
                        .pathParam(OBJECT_ID_MANUFACTURER, parent.getObjectIDManufacturer())
                        .queryParam(VIEW, AS_BUILT)
                    .when()
                        .get(PATH)
                    .then()
                        .assertThat()
                        .statusCode(SC_OK)
                        .extract().as(PartRelationshipsWithInfos.class);

            assertThat(response.getRelationships()).containsExactly(relationship);
        });
    }

    @Test
    public void updateTwoPartsRelationships_success() throws Exception {

        //Arrange
        //Create two relationships with same parent
        var update1 = generateAddedRelationship();
        var update2 = generateAddedRelationshipWithSameParentAs(update1);

        var event = generate.partRelationshipUpdateList(update1, update2);
        var relationship1 = update1.getRelationship();
        var relationship2 = update2.getRelationship();
        var parent1 = relationship1.getParent();

        //Act
        publishUpdateEvent(event);

        //Assert
        await().untilAsserted(() -> {
            var response =
                    given()
                        .pathParam(ONE_ID_MANUFACTURER, parent1.getOneIDManufacturer())
                        .pathParam(OBJECT_ID_MANUFACTURER, parent1.getObjectIDManufacturer())
                        .queryParam(VIEW, AS_BUILT)
                    .when()
                        .get(PATH)
                    .then()
                        .assertThat()
                        .statusCode(SC_OK)
                        .extract().as(PartRelationshipsWithInfos.class);

            assertThat(response.getRelationships())
                    .containsExactlyInAnyOrder(relationship1, relationship2);
        });
    }

    /**
     * An invalid message payload is sent to the dead-letter topic rather than blocking processing,
     * so that a subsequent message in the same partition is processed correctly.
     *
     * @param name test case name, used to generate test display name
     * @param invalidPayload the invalid payload to send before a valid payload
     * @throws Exception on failure
     */
    @ParameterizedTest(name = "{index} {0}")
    @ArgumentsSource(BlankStringsArgumentsProvider.class)
    public void sendWrongMessageThenCorrectMessage_success(String name, Object invalidPayload) throws Exception {

        //Arrange
        var relationshipUpdate = generateAddedRelationship();
        var event = generate.partRelationshipUpdateList(relationshipUpdate);
        var relationship = relationshipUpdate.getRelationship();
        var parent = relationship.getParent();

        //Act
        publishUpdateEvent(invalidPayload);
        publishUpdateEvent(event);

        //Assert
        await().atMost(180, SECONDS).untilAsserted(() -> {
            var response =
                    given()
                        .pathParam(ONE_ID_MANUFACTURER, parent.getOneIDManufacturer())
                        .pathParam(OBJECT_ID_MANUFACTURER, parent.getObjectIDManufacturer())
                        .queryParam(VIEW, AS_BUILT)
                    .when()
                        .get(PATH)
                    .then()
                        .assertThat()
                        .statusCode(SC_OK)
                        .extract().as(PartRelationshipsWithInfos.class);

            assertThat(response.getRelationships()).containsExactly(relationship);
        });
    }

    @Test
    public void updatePartsRelationshipsDuplicateEvent_success() throws Exception {

        //Arrange
        var update1 = generateAddedRelationship();
        var event1 = generate.partRelationshipUpdateList(update1);
        var relationship1 = update1.getRelationship();
        var parent = relationship1.getParent();

        var update2 = generateAddedRelationshipWithSameParentAs(update1);
        var relationship2 = update2.getRelationship();
        var event2 = generate.partRelationshipUpdateList(update2);

        //Act
        publishUpdateEvent(event1);
        publishUpdateEvent(event1);
        publishUpdateEvent(event2);

        //Assert
        await().untilAsserted(() -> {
            var response =
                    given()
                        .pathParam(ONE_ID_MANUFACTURER, parent.getOneIDManufacturer())
                        .pathParam(OBJECT_ID_MANUFACTURER, parent.getObjectIDManufacturer())
                        .queryParam(VIEW, AS_BUILT)
                    .when()
                        .get(PATH)
                    .then()
                        .assertThat()
                        .statusCode(SC_OK)
                        .extract().as(PartRelationshipsWithInfos.class);

            assertThat(response.getRelationships())
                    .containsExactlyInAnyOrder(relationship1, relationship2);
        });
    }

    private PartRelationshipUpdate generateAddedRelationship() {
        return generate.partRelationshipUpdate()
                .toBuilder()
                .withRemove(false)
                .withStage(PartLifecycleStage.BUILD)
                .build();
    }

    private PartRelationshipUpdate generateAddedRelationshipWithSameParentAs(PartRelationshipUpdate update1) {
        return generateAddedRelationship()
                .toBuilder()
                .withRelationship(generateDto.partRelationship().toBuilder().withParent(update1.getRelationship().getParent()).build())
                .build();
    }

    static class BlankStringsArgumentsProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            final var invalidUpdate = generate.partRelationshipUpdate()
                    .toBuilder()
                    .withEffectTime(null)
                    .build();
            return Stream.of(
                    Arguments.of("unsupported payload type", new Faker().lorem().sentence()),
                    Arguments.of("invalid payload", generate.partRelationshipUpdateList(invalidUpdate))
            );
        }
    }
}
