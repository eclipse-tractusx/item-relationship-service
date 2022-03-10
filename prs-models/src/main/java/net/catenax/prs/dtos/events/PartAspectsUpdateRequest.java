//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.prs.dtos.events;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import net.catenax.prs.dtos.Aspect;
import net.catenax.prs.dtos.PartId;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

import static net.catenax.prs.dtos.ValidationConstants.ASPECT_UPDATE_LIST_MAX_SIZE;
import static net.catenax.prs.dtos.ValidationConstants.ASPECT_UPDATE_LIST_MIN_SIZE;

/*** Event for updates to {@link Aspect}s. */
@Schema(description = PartAspectsUpdateRequest.DESCRIPTION)
@Value
@Builder(toBuilder = true, setterPrefix = "with")
@JsonDeserialize(builder = PartAspectsUpdateRequest.PartAspectsUpdateRequestBuilder.class)
@SuppressWarnings("PMD.CommentRequired")
public class PartAspectsUpdateRequest {
    public static final String DESCRIPTION = "Describes an update of a part aspect location.";

    @NotNull
    @Valid
    @Schema(implementation = PartId.class)
    private PartId part;

    @NotEmpty(message = "Aspects list can't be empty. Use remove field to remove part aspects.")
    @Valid
    @Size(min = ASPECT_UPDATE_LIST_MIN_SIZE, max = ASPECT_UPDATE_LIST_MAX_SIZE)
    @Schema(description = "Aspect location.")
    private List<Aspect> aspects;

    @Schema(description =
            "<ul>"
                    + "   <li>TRUE if the aspect URLs are to be deleted from the part</li>"
                    + "   <li>FALSE otherwise (“normal case” - an aspect URL is added to a part).</li>"
                    + "</ul>")
    private boolean remove;

    @Past
    @NotNull
    @Schema(description = "Instant at which the update was applied")
    private Instant effectTime;
}
