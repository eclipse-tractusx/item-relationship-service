/********************************************************************************
 * Copyright (c) 2022,2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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
package org.eclipse.tractusx.irs.semanticshub;

import java.util.List;

import org.eclipse.tractusx.irs.SemanticModelNames;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Semantics Hub Rest Client Stub used in local environment
 */
@Service
@Profile({ "local",
           "test"
})
class SemanticsHubClientLocalStub implements SemanticsHubClient {

    public static final String MODEL_TYPE = "SAMM";
    public static final String MODEL_STATUS = "RELEASED";

    @Override
    public String getModelJsonSchema(final String urn) {
        return "{" + "  \"$schema\": \"http://json-schema.org/draft-07/schema#\"," + "  \"type\": \"integer\"" + "}";
    }

    @Override
    public List<AspectModel> getAllAspectModels() {
        return List.of(
                new AspectModel(SemanticModelNames.SERIAL_PART_3_0_0, "3.0.0", "SerialPart", MODEL_TYPE, MODEL_STATUS),
                new AspectModel(SemanticModelNames.SINGLE_LEVEL_BOM_AS_BUILT_3_0_0, "3.0.0", "SingleLevelBomAsBuilt",
                        MODEL_TYPE, MODEL_STATUS),
                new AspectModel(SemanticModelNames.PART_AS_SPECIFIED_2_0_0, "2.0.0", "PartAsSpecified", MODEL_TYPE,
                        MODEL_STATUS),
                new AspectModel(SemanticModelNames.PART_AS_PLANNED_1_0_1, "1.0.1", "PartAsPlanned", MODEL_TYPE,
                        MODEL_STATUS));
    }
}
