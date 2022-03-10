//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.prs.validators;

import net.catenax.prs.annotations.ExcludeFromCodeCoverageGeneratedReport;
import net.catenax.prs.annotations.ValueOfEnum;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Generic validator for Enum values.
 * This validator was added so that we can use String data type in place of Enum for API input request object. As spring BindException details are not that user-friendly when mapping an input which is not value of the Enum.
 */
@ExcludeFromCodeCoverageGeneratedReport
@SuppressWarnings({"PMD.CommentSize", "PMD.BeanMembersShouldSerialize"})
public class ValueOfEnumValidator implements ConstraintValidator<ValueOfEnum, CharSequence> {

    /**
     * Accepted values for Enum under validation.
     */
    private List<String> acceptedValues;

    @Override
    public void initialize(final ValueOfEnum annotation) {
        acceptedValues = Stream.of(annotation.enumClass().getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isValid(final CharSequence value, final ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        return acceptedValues.contains(value.toString());
    }
}
