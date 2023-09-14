/********************************************************************************
 * Copyright (c) 2021,2022,2023
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2023: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
 * Copyright (c) 2021,2022,2023 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.irs.aaswrapper.job;

import lombok.Value;
import org.eclipse.tractusx.irs.component.PartChainIdentificationKey;
import org.eclipse.tractusx.irs.connector.job.DataRequest;

/**
 * Data Request for CatenaX IDs
 */
@Value
public class ItemDataRequest implements DataRequest {

    private final PartChainIdentificationKey itemId;
    private final Integer depth;

    public static ItemDataRequest rootNode(final PartChainIdentificationKey itemId) {
        return new ItemDataRequest(itemId, 0);
    }

    public static ItemDataRequest nextDepthNode(final PartChainIdentificationKey itemId, final Integer currentDepth) {
        return new ItemDataRequest(itemId, currentDepth + 1);
    }
}
