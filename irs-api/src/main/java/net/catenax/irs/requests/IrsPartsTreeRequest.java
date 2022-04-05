//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//

package net.catenax.irs.requests;

import static io.swagger.v3.oas.annotations.enums.ParameterIn.PATH;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import net.catenax.irs.controllers.ApiErrorsConstants;

/**
 * IrsPartsTree request object
 */
@Value
public class IrsPartsTreeRequest extends IrsPartsTreeRequestBase {

    @Pattern(regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
            message = ApiErrorsConstants.NOT_BLANK)
    @NotBlank
    @Size(min = 1, max = 16)
    @Parameter(description = "Registers and starts a AAS crawler job for given {globalAssetId}", in = PATH,
            required = true, schema = @Schema(implementation = String.class))
    private String globalAssetId;

    /**
     * @param globalAssetId see {@link #getGlobalAssetId()}
     * @param bomLifecycle  see {@link #getBomLifecycle()}
     * @param aspect        see {@link #getAspect()}
     * @param dept          see {@link #getDept()}
     */
    @Builder(toBuilder = true)
    public IrsPartsTreeRequest(final String globalAssetId, final String bomLifecycle, final String aspect,
            final Integer dept, final String direction) {
        super(bomLifecycle, aspect, dept, direction);
        this.globalAssetId = globalAssetId;
    }

}
