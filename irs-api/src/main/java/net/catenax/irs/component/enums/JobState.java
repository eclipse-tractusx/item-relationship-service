//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.component.enums;

/**
 * Represents the state of the current job
 */
public enum JobState {
    UNSAVED,
    INITIAL,
    IN_PROGRESS,
    TRANSFERS_FINISHED,
    COMPLETED,
    ERROR
}
