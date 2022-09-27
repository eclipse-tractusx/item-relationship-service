/********************************************************************************
 * Copyright (c) 2021,2022
 *       2022: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 * Copyright (c) 2021,2022 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0. *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/
package org.eclipse.tractusx.esr.irs.model.shell;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 *  Shell
 */
@Value
@Builder(toBuilder = true)
@Jacksonized
public class Shell {

    private String identification;
    private List<IdentifierKeyValuePair> specificAssetIds;
    private ListOfValues globalAssetId;
    private List<SubmodelDescriptor> submodelDescriptors;

    @JsonIgnore
    private static final Pattern BPN_RGX = Pattern.compile("(BPN)[LSA][\\w\\d]{10}[\\w\\d]{2}");

    /**
     * @return ManufacturerId value from Specific Asset Ids
     */
    public Optional<String> findManufacturerIdIfValid() {
        return this.specificAssetIds
                .stream()
                .filter(assetId -> "manufacturerId".equals(assetId.getKey()))
                .map(IdentifierKeyValuePair::getValue)
                .filter(BPN_RGX.asPredicate())
                .findFirst();
    }

}
