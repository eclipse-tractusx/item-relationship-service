package net.catenax.prs.requests;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static java.util.function.UnaryOperator.identity;
import static org.assertj.core.api.Assertions.assertThat;

public class PartsTreeByVinRequestTests extends RequestTestBase {

    PartsTreeByVinRequest sut = generateRequest.byVin();

    @ParameterizedTest(name = "{0}")
    @MethodSource("mutators")
    public void validate(String testName, UnaryOperator<PartsTreeByVinRequest.PartsTreeByVinRequestBuilder> mutator, String expectedViolationPath) {
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

                args("Depth min 1 [1]", b -> b.depth(faker.number().numberBetween(Integer.MIN_VALUE, 0)), "depth"),
                args("Depth min 1 [2]", b -> b.depth(0), "depth"),

                args("Vin not null", b -> b.vin(null), "vin"),
                args("Vin not empty", b -> b.vin(EMPTY), "vin"),
                args("Vin not blank", b -> b.vin(faker.regexify(WHITESPACE_REGEX)), "vin"),
                args("Vin must be size 17 [1]", b -> b.vin(faker.lorem().characters(1, 16)), "vin"),
                args("Vin must be size 17 [2]", b -> b.vin(faker.lorem().characters(16)), "vin"),
                args("Vin must be size 17 [3]", b -> b.vin(faker.lorem().characters(18, 100)), "vin"),
                args("Vin must be size 17 [4]", b -> b.vin(faker.lorem().characters(18)), "vin")
        );
    }

    private static Arguments args(String testName,
                                  UnaryOperator<PartsTreeByVinRequest.PartsTreeByVinRequestBuilder> mutator,
                                  String expectedViolationPath) {
        return Arguments.of(testName, mutator, expectedViolationPath);
    }
}
