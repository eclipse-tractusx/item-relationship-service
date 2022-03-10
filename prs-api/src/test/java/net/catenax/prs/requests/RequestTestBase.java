package net.catenax.prs.requests;

import com.github.javafaker.Faker;

import javax.validation.Validation;
import javax.validation.Validator;

abstract class RequestTestBase {

    protected static final Faker faker = new Faker();
    /**
     * Instance of {@link Validator} based on the default Jakarta Bean Validation.
     */
    protected static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    /**
     * This will match any of these whitespaces
     * e.g. space (_), the tab (\t), the new line (\n) and the carriage return (\r).
     */
    protected static final String WHITESPACE_REGEX = "\\s";
    /**
     * Empty string as a constant.
     */
    protected static final String EMPTY = "";
    /**
     * Object Mother to generate prs api request data for testing.
     */
    protected RequestMother generateRequest = new RequestMother();
}
