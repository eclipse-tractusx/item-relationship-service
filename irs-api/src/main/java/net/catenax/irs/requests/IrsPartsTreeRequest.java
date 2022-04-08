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
import net.catenax.irs.connector.annotations.ExcludeFromCodeCoverageGeneratedReport;
import net.catenax.irs.controllers.ApiErrorsConstants;
import net.catenax.irs.controllers.IrsApiConstants;
import net.catenax.irs.controllers.IrsApiExamplesUtils;

/**
 * IrsPartsTree request object
 */
@Value
@ExcludeFromCodeCoverageGeneratedReport
public class IrsPartsTreeRequest extends IrsPartsTreeRequestBase {

    @Pattern(regexp = IrsApiConstants.GLOBAL_ASSET_ID_REGEX,
            message = ApiErrorsConstants.NOT_BLANK)
    @NotBlank
    @Size(min = IrsApiConstants.JOB_ID_SIZE, max = IrsApiConstants.JOB_ID_SIZE)
    @Parameter(description = "Readable ID of manufacturer including plant.", in = PATH,
            required = true, example = IrsApiExamplesUtils.GLOBAL_ASSET_ID_EXAMPLE, schema = @Schema(implementation = String.class))
    private String globalAssetId;

    /**
     * @param globalAssetId see {@link #getGlobalAssetId()}
     * @param bomLifecycle  see {@link #getBomLifecycle()}
     * @param aspects        see {@link #getAspects()}
     * @param depth         see {@link #getDepth()}
     * @param direction         see {@link #getDirection()}
     */
    @Builder(toBuilder = true)
    public IrsPartsTreeRequest(final String globalAssetId, final String bomLifecycle, final String aspects,
            final Integer depth, final String direction) {
        super(bomLifecycle, aspects, depth, direction);
        this.globalAssetId = globalAssetId;
    }

}
