//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.connector.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import net.catenax.irs.connector.annotations.ExcludeFromCodeCoverageGeneratedReport;

import java.time.LocalDateTime;

import static net.catenax.irs.dtos.ValidationConstants.INPUT_FIELD_MAX_LENGTH;
import static net.catenax.irs.dtos.ValidationConstants.INPUT_FIELD_MIN_LENGTH;

/**
 * Parameter object for
 * getPartsTree
 * REST operation.
 */
@Value
@Jacksonized // Makes the class deserializable using lombok builder.
@Builder(toBuilder = true)
@ExcludeFromCodeCoverageGeneratedReport
public class JobsTreeByCatenaXIdRequest {

    /**
     * Unique ID of an Asset aka a Digital Twin aka AAS
     */
    @NotBlank
    @Size(min = INPUT_FIELD_MIN_LENGTH, max = INPUT_FIELD_MAX_LENGTH)
    private String childCatenaXId;

    /**
     * Unique ID of an Asset aka a Digital Twin aka AAS
     */
    @NotBlank
    private String lifecycleContext;

    /**
     * PartsTree View to retrieve.
     */
    @NotBlank
    private LocalDateTime assembledOn;

    /**
     * PartsTree View to retrieve.
     */
    @NotBlank
    private LocalDateTime lastModifiedOn;

    /**
     * Max depth of the returned tree, if {@literal null}, max depth is returned. May be {@literal null}.
     */
    @Min(1)
    private final Integer depth;
}

