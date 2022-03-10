package net.catenax.prs.dtos;

import com.github.javafaker.Faker;
import net.catenax.prs.dtos.PartRelationship.PartRelationshipBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.UnaryOperator.identity;
import static org.assertj.core.api.Assertions.assertThat;

public class PartRelationshipTests {

    static final Faker faker = new Faker();
    static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    /**
     * This will match any of these whitespaces
     * e.g. space (_), the tab (\t), the new line (\n) and the carriage return (\r).
     */
    static final String WHITESPACE_REGEX = "\\s";
    /**
     * Empty string as a constant.
     */
    static final String EMPTY = "";

    PartRelationship sut = partRelationship();

    @Test
    public void validateParentChildHaveUniquePartId() {
        //Arrange
        var partId = partId();
        sut = sut.toBuilder()
                .withChild(partId)
                .withParent(partId)
                .build();
        //Act
        var violations = validator.validate(sut);
        //Assert
        var violationMessages = violations.stream().map(ConstraintViolation::getMessage);
        assertThat(violationMessages).containsExactly("Parent and Child part identifier must not be same");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("mutators")
    public void validate(String testName, UnaryOperator<PartRelationshipBuilder> mutator, String expectedViolationPath) {
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

                args("Parent not null", b -> b.withParent(null), "parent"),

                args("Child not null", b -> b.withChild(null), "child"),

                args("Parent OneIDManufacturer not null", b -> b.withParent(partId().toBuilder().withOneIDManufacturer(null).build()), "parent.oneIDManufacturer"),
                args("Parent OneIDManufacturer not empty", b -> b.withParent(partId().toBuilder().withOneIDManufacturer(EMPTY).build()), "parent.oneIDManufacturer"),
                args("Parent OneIDManufacturer not blank", b -> b.withParent(partId().toBuilder().withOneIDManufacturer(faker.regexify(WHITESPACE_REGEX)).build()), "parent.oneIDManufacturer"),
                args("Parent OneIDManufacturer max 10000 [1]", b -> b.withParent(partId().toBuilder().withOneIDManufacturer(faker.lorem().characters(10001)).build()), "parent.oneIDManufacturer"),
                args("Parent OneIDManufacturer max 10000 [2]", b -> b.withParent(partId().toBuilder().withOneIDManufacturer(faker.lorem().characters(10001, 100000)).build()), "parent.oneIDManufacturer"),

                args("Parent ObjectIDManufacturer not null", b -> b.withParent(partId().toBuilder().withObjectIDManufacturer(null).build()), "parent.objectIDManufacturer"),
                args("Parent ObjectIDManufacturer not empty", b -> b.withParent(partId().toBuilder().withObjectIDManufacturer(EMPTY).build()), "parent.objectIDManufacturer"),
                args("Parent ObjectIDManufacturer not blank", b -> b.withParent(partId().toBuilder().withObjectIDManufacturer(faker.regexify(WHITESPACE_REGEX)).build()), "parent.objectIDManufacturer"),
                args("Parent ObjectIDManufacturer max 10000 [1]", b -> b.withParent(partId().toBuilder().withObjectIDManufacturer(faker.lorem().characters(10001)).build()), "parent.objectIDManufacturer"),
                args("Parent ObjectIDManufacturer max 10000 [2]", b -> b.withParent(partId().toBuilder().withObjectIDManufacturer(faker.lorem().characters(10001, 100000)).build()), "parent.objectIDManufacturer"),

                args("Child OneIDManufacturer not null", b -> b.withChild(partId().toBuilder().withOneIDManufacturer(null).build()), "child.oneIDManufacturer"),
                args("Child OneIDManufacturer not empty", b -> b.withChild(partId().toBuilder().withOneIDManufacturer(EMPTY).build()), "child.oneIDManufacturer"),
                args("Child OneIDManufacturer not blank", b -> b.withChild(partId().toBuilder().withOneIDManufacturer(faker.regexify(WHITESPACE_REGEX)).build()), "child.oneIDManufacturer"),
                args("Child OneIDManufacturer max 10000 [1]", b -> b.withChild(partId().toBuilder().withOneIDManufacturer(faker.lorem().characters(10001)).build()), "child.oneIDManufacturer"),
                args("Child OneIDManufacturer max 10000 [2]", b -> b.withChild(partId().toBuilder().withOneIDManufacturer(faker.lorem().characters(10001, 100000)).build()), "child.oneIDManufacturer"),

                args("Child ObjectIDManufacturer not null", b -> b.withChild(partId().toBuilder().withObjectIDManufacturer(null).build()), "child.objectIDManufacturer"),
                args("Child ObjectIDManufacturer not empty", b -> b.withChild(partId().toBuilder().withObjectIDManufacturer(EMPTY).build()), "child.objectIDManufacturer"),
                args("Child ObjectIDManufacturer not blank", b -> b.withChild(partId().toBuilder().withObjectIDManufacturer(faker.regexify(WHITESPACE_REGEX)).build()), "child.objectIDManufacturer"),
                args("Child ObjectIDManufacturer max 10000 [1]", b -> b.withChild(partId().toBuilder().withObjectIDManufacturer(faker.lorem().characters(10001)).build()), "child.objectIDManufacturer"),
                args("Child ObjectIDManufacturer max 10000 [2]", b -> b.withChild(partId().toBuilder().withObjectIDManufacturer(faker.lorem().characters(10001, 100000)).build()), "child.objectIDManufacturer")
        );
    }

    private static Arguments args(String testName,
                                  UnaryOperator<PartRelationshipBuilder> mutator,
                                  String expectedViolationPath) {
        return Arguments.of(testName, mutator, expectedViolationPath);
    }

    /**
     * Generate {@link PartRelationship} with random values.
     *
     * @return a {@link PartRelationship} with random identifiers.
     */
    private static PartRelationship partRelationship() {
        return PartRelationship.builder()
                .withParent(partId())
                .withChild(partId())
                .build();
    }

    /**
     * Generate {@link PartId} with random values.
     *
     * @return a {@link PartId} with random identifiers.
     */
    private static PartId partId() {
        return PartId.builder()
                .withOneIDManufacturer(faker.company().name())
                .withObjectIDManufacturer(faker.lorem().characters(10, 20))
                .build();
    }
}
