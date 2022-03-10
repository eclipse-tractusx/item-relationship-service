package net.catenax.prs.requests;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static java.util.function.UnaryOperator.identity;
import static org.assertj.core.api.Assertions.assertThat;

public class PartsTreeByObjectIdRequestTests extends RequestTestBase {

    PartsTreeByObjectIdRequest sut = generateRequest.byObjectId();

    @ParameterizedTest(name = "{0}")
    @MethodSource("mutators")
    public void validate(String testName, UnaryOperator<PartsTreeByObjectIdRequest.PartsTreeByObjectIdRequestBuilder> mutator, String expectedViolationPath) {
        sut = mutator.apply(sut.toBuilder()).build();
        //Act
        var violations = validator.validate(sut);
        //Assert
        if (expectedViolationPath == null) {
            assertThat(violations.isEmpty()).isTrue();
        } else {
            var violationPaths = violations.stream().map(v -> v.getPropertyPath().toString());
            assertThat(violationPaths).contains(expectedViolationPath);
        }
    }

    private static Stream<Arguments> mutators() {
        return Stream.of(
                args("Valid", identity(), null),

                args("Invalid view", b -> b.view(faker.lorem().word()), "view"),
                args("View not null", b -> b.view(null), "view"),
                args("View not empty", b -> b.view(EMPTY), "view"),
                args("View not blank", b -> b.view(faker.regexify(WHITESPACE_REGEX)), "view"),

                args("Aspect not empty", b -> b.aspect(EMPTY), "aspect"),
                args("Aspect not blank", b -> b.aspect(faker.regexify(WHITESPACE_REGEX)), "aspect"),

                args("Depth min 1", b -> b.depth(faker.number().numberBetween(Integer.MIN_VALUE, 0)), "depth"),
                args("oneIDManufacturer not null", b -> b.oneIDManufacturer(null), "oneIDManufacturer"),
                args("oneIDManufacturer not empty", b -> b.oneIDManufacturer(EMPTY), "oneIDManufacturer"),
                args("oneIDManufacturer not blank", b -> b.oneIDManufacturer(faker.regexify(WHITESPACE_REGEX)), "oneIDManufacturer"),
                args("oneIDManufacturer max 10000 [1]", b -> b.oneIDManufacturer(faker.lorem().characters(10001)), "oneIDManufacturer"),
                args("oneIDManufacturer max 10000 [2]", b -> b.oneIDManufacturer(faker.lorem().characters(10001, 100000)), "oneIDManufacturer"),

                args("objectIDManufacturer not null", b -> b.objectIDManufacturer(null), "objectIDManufacturer"),
                args("objectIDManufacturer not empty", b -> b.objectIDManufacturer(EMPTY), "objectIDManufacturer"),
                args("objectIDManufacturer not blank", b -> b.objectIDManufacturer(faker.regexify(WHITESPACE_REGEX)), "objectIDManufacturer"),
                args("objectIDManufacturer max 10000 [1]", b -> b.objectIDManufacturer(faker.lorem().characters(10001)), "objectIDManufacturer"),
                args("objectIDManufacturer max 10000 [2]", b -> b.objectIDManufacturer(faker.lorem().characters(10001, 100000)), "objectIDManufacturer")
        );
    }

    private static Arguments args(String testName,
                                  UnaryOperator<PartsTreeByObjectIdRequest.PartsTreeByObjectIdRequestBuilder> mutator,
                                  String expectedViolationPath) {
        return Arguments.of(testName, mutator, expectedViolationPath);
    }
}
