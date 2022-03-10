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

import net.catenax.prs.annotations.UniquePartIdentifierForParentChild;
import net.catenax.prs.dtos.PartRelationship;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validator for {@link net.catenax.prs.dtos.PartRelationship}.
 */
public class UniquePartIdentifierForParentChildValidator implements ConstraintValidator<UniquePartIdentifierForParentChild, PartRelationship> {

    /**
     * Validates parent and child {@link net.catenax.prs.dtos.PartId} must not be same.
     *
     * Null {@link net.catenax.prs.dtos.PartId} for Parent or Child is considered valid input here
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
