//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package org.eclipse.tractusx.irs.component;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.tractusx.irs.component.enums.AspectType;
import org.eclipse.tractusx.irs.component.enums.BomLifecycle;
import org.eclipse.tractusx.irs.component.enums.Direction;

/**
 * Job parameter of job processing
 */
@Schema(description = "Job parameter of job processing.")
@Value
@Builder(toBuilder = true)
@AllArgsConstructor
@Jacksonized
public class JobParameter {

    @Schema(implementation = BomLifecycle.class)
    private BomLifecycle bomLifecycle;

    @Schema(implementation = AspectType.class)
    @Singular
    private List<AspectType> aspects;

    @Schema(implementation = Integer.class)
    private Integer depth;

    @Schema(implementation = Direction.class)
    private Direction direction;

    @Schema(implementation = Boolean.class)
    private Boolean collectAspects;

}
