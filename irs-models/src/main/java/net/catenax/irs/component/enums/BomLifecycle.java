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

import io.swagger.v3.oas.annotations.media.Schema;
import net.catenax.irs.annotations.ExcludeFromCodeCoverageGeneratedReport;
import net.catenax.irs.dtos.ItemLifecycleStage;

/***
 * API type for the view of the items tree to be returned by a query.
 *
 * @see ItemLifecycleStage
 */
@ExcludeFromCodeCoverageGeneratedReport
@Schema(description = "View defining which data of the PartsTree is retrieved.")
public enum BomLifecycle {
    @Schema(description = "The view of the ItemsTree as the vehicle was assembled.") AS_BUILT("asBuild")
    ,@Schema(description = "The view of the PartsTree ... lifecycle.") AS_MAINTAINED("asMaintained")
    ,@Schema(description = "TThe view of the PartsTree ... lifecycle.") AS_PLANNED("asPlanned")
    ,@Schema(description = "TThe view of the PartsTree ... lifecycle.") AS_DESIGNED("asDesigned")
    ,@Schema(description = "The view of the PartsTree ... lifecycle.") AS_ORDERED("asOrdered")
    ,@Schema(description = "The view of the PartsTree ... lifecycle.") AS_RECYCLED("asRecycled");

    private final String value;

    BomLifecycle(final String value) {
        this.value = value;
    }

    /**
     * of as a substitute/alias for valueOf handling the default value
     *
     * @param value see {@link #value}
     * @return the corresponding BomLifecycle
     */
    public static BomLifecycle value(final String value) {
        return BomLifecycle.valueOf(value);
    }

    /**
     * @return convert BomLifecycle to string value
     */
    @Override
    public String toString() {
        return value;
    }
}
