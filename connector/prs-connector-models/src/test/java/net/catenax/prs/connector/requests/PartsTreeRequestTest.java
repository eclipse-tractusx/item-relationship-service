package net.catenax.prs.connector.requests;

import jakarta.validation.Validator;
import net.catenax.prs.connector.requests.PartsTreeRequest.PartsTreeRequestBuilder;
import net.catenax.prs.connector.testing.ValidatorUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static java.util.function.UnaryOperator.identity;
import static net.catenax.prs.connector.testing.SetOfConstraintViolationsAssertions.assertThat;


class PartsTreeRequestTest {

    private static final String EMPTY = "";
    static Validator validator = ValidatorUtils.createValidator();

    PartsTreeRequest sut = RequestMother.generatePartsTreeRequest();

    @ParameterizedTest(name = "{0}")
    @MethodSource("mutators")
    void validate(String testName, UnaryOperator<PartsTreeRequestBuilder> mutator, String expectedViolationPath) {
        sut = mutator.apply(sut.toBuilder()).build();
        // Act
        var response = validator.validate(sut);
        // Assert
        if (expectedViolationPath != null) {
            assertThat(response).hasViolationWithPath(expectedViolationPath);
        } else {
            assertThat(response).hasNoViolations();
        }
    }

    static Stream<Arguments> mutators() {
        return Stream.of(
                args("valid", identity(), null),

                args("byObjectIdRequest not null", b -> b.byObjectIdRequest(null), "byObjectIdRequest"),
                args("byObjectIdRequest valid", b -> b.byObjectIdRequest(b.build().getByObjectIdRequest().toBuilder().objectIDManufacturer(null).build()), "byObjectIdRequest.objectIDManufacturer")
        );
    }

    private static Arguments args(String testName,
                                  UnaryOperator<PartsTreeRequestBuilder> mutator,
                                  String expectedViolationPath) {
        return Arguments.of(testName, mutator, expectedViolationPath);
    }
}
