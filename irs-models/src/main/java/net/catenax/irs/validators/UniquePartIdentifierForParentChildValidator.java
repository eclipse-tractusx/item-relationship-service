//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import net.catenax.irs.annotations.UniquePartIdentifierForParentChild;
import net.catenax.irs.dtos.PartId;
import net.catenax.irs.dtos.PartRelationship;

/**
 * Validator for {@link PartRelationship}.
 */
public class UniquePartIdentifierForParentChildValidator
        implements ConstraintValidator<UniquePartIdentifierForParentChild, PartRelationship> {

    /**
     * Validates parent and child {@link PartId} must not be same.
     * Null {@link PartId} for Parent or Child is considered valid input here
     * as this validator focuses only on having a unique part identifier.
     */
    @Override
    public boolean isValid(final PartRelationship partRelationship, final ConstraintValidatorContext context) {
        if (partRelationship == null || partRelationship.getParent() == null || partRelationship.getChild() == null) {
            return true;
        }

        return !partRelationship.getParent().equals(partRelationship.getChild());
    }
}
