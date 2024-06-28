/********************************************************************************
 * Copyright (c) 2022,2024
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2024: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2024 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.irs.component;

import static org.eclipse.tractusx.irs.component.RegisterBatchOrder.RegisterBatchOrderConstants.DEFAULT_BATCH_SIZE;
import static org.eclipse.tractusx.irs.component.RegisterBatchOrder.RegisterBatchOrderConstants.DEFAULT_BATCH_SIZE_DESC;
import static org.eclipse.tractusx.irs.component.RegisterBatchOrder.RegisterBatchOrderConstants.DEFAULT_JOB_TIMEOUT;
import static org.eclipse.tractusx.irs.component.RegisterBatchOrder.RegisterBatchOrderConstants.DEFAULT_JOB_TIMEOUT_DESC;
import static org.eclipse.tractusx.irs.component.RegisterBatchOrder.RegisterBatchOrderConstants.DEFAULT_TIMEOUT;
import static org.eclipse.tractusx.irs.component.RegisterBatchOrder.RegisterBatchOrderConstants.DEFAULT_TIMEOUT_DESC;
import static org.eclipse.tractusx.irs.component.RegisterBatchOrder.RegisterBatchOrderConstants.MAX_BATCH_SIZE;
import static org.eclipse.tractusx.irs.component.RegisterBatchOrder.RegisterBatchOrderConstants.MAX_BATCH_SIZE_DESC;
import static org.eclipse.tractusx.irs.component.RegisterBatchOrder.RegisterBatchOrderConstants.MAX_JOB_TIMEOUT;
import static org.eclipse.tractusx.irs.component.RegisterBatchOrder.RegisterBatchOrderConstants.MAX_JOB_TIMEOUT_DESC;
import static org.eclipse.tractusx.irs.component.RegisterBatchOrder.RegisterBatchOrderConstants.MAX_TIMEOUT;
import static org.eclipse.tractusx.irs.component.RegisterBatchOrder.RegisterBatchOrderConstants.MAX_TIMEOUT_DESC;
import static org.eclipse.tractusx.irs.component.RegisterBatchOrder.RegisterBatchOrderConstants.MAX_TREE_DEPTH;
import static org.eclipse.tractusx.irs.component.RegisterBatchOrder.RegisterBatchOrderConstants.MAX_TREE_DEPTH_DESC;
import static org.eclipse.tractusx.irs.component.RegisterBatchOrder.RegisterBatchOrderConstants.MIN_BATCH_SIZE;
import static org.eclipse.tractusx.irs.component.RegisterBatchOrder.RegisterBatchOrderConstants.MIN_BATCH_SIZE_DESC;
import static org.eclipse.tractusx.irs.component.RegisterBatchOrder.RegisterBatchOrderConstants.MIN_JOB_TIMEOUT;
import static org.eclipse.tractusx.irs.component.RegisterBatchOrder.RegisterBatchOrderConstants.MIN_JOB_TIMEOUT_DESC;
import static org.eclipse.tractusx.irs.component.RegisterBatchOrder.RegisterBatchOrderConstants.MIN_TIMEOUT;
import static org.eclipse.tractusx.irs.component.RegisterBatchOrder.RegisterBatchOrderConstants.MIN_TIMEOUT_DESC;
import static org.eclipse.tractusx.irs.component.RegisterBatchOrder.RegisterBatchOrderConstants.MIN_TREE_DEPTH;
import static org.eclipse.tractusx.irs.component.RegisterBatchOrder.RegisterBatchOrderConstants.MIN_TREE_DEPTH_DESC;

import java.util.List;
import java.util.Set;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.tractusx.irs.component.enums.BatchStrategy;
import org.eclipse.tractusx.irs.component.enums.BomLifecycle;
import org.eclipse.tractusx.irs.component.enums.Direction;
import org.eclipse.tractusx.irs.validators.Mod10;
import org.hibernate.validator.constraints.URL;

/**
 * Request body for registering a new Batch Order
 */
@Schema(description = "Request body for registering a new Batch Order.")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SuppressWarnings({ "PMD.TooManyStaticImports",
                    "PMD.ExcessiveImports",
})
public class RegisterBatchOrder {

    private static final String ASPECT_MODEL_REGEX = "^(urn:[bs]amm:.*\\d\\.\\d\\.\\d)?(#)?(\\w+)?$";

    @NotEmpty
    @Valid
    @ArraySchema(schema = @Schema(description = "Keys array contains required attributes for identify part chain entry node ", implementation = PartChainIdentificationKey.class), maxItems = Integer.MAX_VALUE)
    private Set<PartChainIdentificationKey> keys;

    @Schema(description = "BoM Lifecycle of the result tree.", implementation = BomLifecycle.class)
    private BomLifecycle bomLifecycle;

    @ArraySchema(arraySchema = @Schema(description = "List of aspect names that will be collected if \\<collectAspects\\> flag is set to true.",
                                       example = "[\"urn:samm:io.catenax.single_level_bom_as_built:3.0.0#SingleLevelBomAsBuilt\"]",
                                       implementation = String.class, pattern = ASPECT_MODEL_REGEX), maxItems = Integer.MAX_VALUE)
    private List<@Pattern(regexp = ASPECT_MODEL_REGEX) String> aspects;

    @Schema(implementation = Integer.class, minimum = MIN_TREE_DEPTH_DESC, maximum = MAX_TREE_DEPTH_DESC,
            description = "Max depth of the item graph returned. If no depth is set item graph with max depth is returned.")
    @Min(MIN_TREE_DEPTH)
    @Max(MAX_TREE_DEPTH)
    private Integer depth;

    @Schema(implementation = Direction.class, defaultValue = Direction.DirectionConstants.DOWNWARD)
    private Direction direction;

    @Schema(description = "Flag to specify whether aspects should be requested and collected. Default is false.")
    private boolean collectAspects;

    @Schema(description = "Flag to specify whether BPNs should be collected and resolved via the configured BPDM URL. Default is false.", deprecated = true)
    private boolean lookupBPNs;

    @URL
    @Schema(description = "Callback url to notify requestor when job processing is finished. There are four uri variable placeholders that can be used: orderId, batchId, orderState and batchState.",
            example = "https://hostname.com/callback?orderId={orderId}&batchId={batchId}&orderState={orderState}&batchState={batchState}")
    private String callbackUrl;

    @Schema(implementation = Integer.class, minimum = MIN_BATCH_SIZE_DESC, maximum = MAX_BATCH_SIZE_DESC, defaultValue = DEFAULT_BATCH_SIZE_DESC,
            description = "Size of the batch.")
    @Min(MIN_BATCH_SIZE)
    @Max(MAX_BATCH_SIZE)
    @Mod10(message = "Batch size value must be mod 10 compliant")
    private Integer batchSize = DEFAULT_BATCH_SIZE;

    @Schema(implementation = Integer.class, minimum = MIN_TIMEOUT_DESC, maximum = MAX_TIMEOUT_DESC, defaultValue = DEFAULT_TIMEOUT_DESC,
            description = "Timeout in seconds for the complete batch order processing.")
    @Min(MIN_TIMEOUT)
    @Max(MAX_TIMEOUT)
    private Integer timeout = DEFAULT_TIMEOUT;

    @Schema(implementation = Integer.class, minimum = MIN_JOB_TIMEOUT_DESC, maximum = MAX_JOB_TIMEOUT_DESC, defaultValue = DEFAULT_JOB_TIMEOUT_DESC,
            description = "Timeout in seconds for each job processing inside the complete order.")
    @Min(MIN_JOB_TIMEOUT)
    @Max(MAX_JOB_TIMEOUT)
    private Integer jobTimeout = DEFAULT_JOB_TIMEOUT;

    @Schema(implementation = BatchStrategy.class/*, defaultValue = BatchStrategy.PRESERVE_BATCH_JOB_ORDER.name()*/, description = "The strategy how the batch is processed internally in IRS.")
    private BatchStrategy batchStrategy;


    /**
     * Returns requested depth if provided, otherwise MAX_TREE_DEPTH value
     *
     * @return depth
     */
    public int getDepth() {
        return depth == null ? MAX_TREE_DEPTH : depth;
    }

    /**
     * Validation constants
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    /* package */ static final class RegisterBatchOrderConstants {
        /* package */ static final String MIN_TREE_DEPTH_DESC = "1";
        /* package */ static final String MAX_TREE_DEPTH_DESC = "100";
        /* package */ static final int MIN_TREE_DEPTH = 1;
        /* package */ static final int MAX_TREE_DEPTH = 100;
        /* package */ static final String MIN_BATCH_SIZE_DESC = "10";
        /* package */ static final String MAX_BATCH_SIZE_DESC = "100";
        /* package */ static final String DEFAULT_BATCH_SIZE_DESC = "20";
        /* package */ static final int MIN_BATCH_SIZE = 10;
        /* package */ static final int MAX_BATCH_SIZE = 100;
        /* package */ static final int DEFAULT_BATCH_SIZE = 20;
        /* package */ static final String MIN_TIMEOUT_DESC = "60";
        /* package */ static final String MAX_TIMEOUT_DESC = "86400";
        /* package */ static final String DEFAULT_TIMEOUT_DESC = "43200";
        /* package */ static final int MIN_TIMEOUT = 60;
        /* package */ static final int MAX_TIMEOUT = 86_400;
        /* package */ static final int DEFAULT_TIMEOUT = 43_200;
        /* package */ static final String MIN_JOB_TIMEOUT_DESC = "60";
        /* package */ static final String MAX_JOB_TIMEOUT_DESC = "7200";
        /* package */ static final String DEFAULT_JOB_TIMEOUT_DESC = "3600";
        /* package */ static final int MIN_JOB_TIMEOUT = 60;
        /* package */ static final int MAX_JOB_TIMEOUT = 7200;
        /* package */ static final int DEFAULT_JOB_TIMEOUT = 3600;
        /* package */ static final String GLOBAL_ASSET_ID_REGEX = "^urn:uuid:[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
    }
}
