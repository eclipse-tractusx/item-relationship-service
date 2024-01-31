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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

/**
 * Provide descriptions to request parts
 */
@Schema(description = "Provide descriptions to request parts.")
@Value
@Builder(toBuilder = true)
@JsonDeserialize(builder = Description.DescriptionBuilder.class)
public class Description {

    public static final int LANGUAGE_MAX_LENGTH = 3;
    public static final int DESCRIPTION_MAX_LENGTH = 4000;

    @Schema(description = "Language used for description.", example = "en", implementation = String.class,
            minLength = 0, maxLength = LANGUAGE_MAX_LENGTH)
    private String language;

    @Schema(description = "Description text.", example = "The shell for a vehicle", implementation = String.class,
            minLength = 0, maxLength = DESCRIPTION_MAX_LENGTH)
    private String text;

    /**
     * Builder for Description class
     */
    @JsonPOJOBuilder(withPrefix = "")
    public static class DescriptionBuilder {
    }
}
