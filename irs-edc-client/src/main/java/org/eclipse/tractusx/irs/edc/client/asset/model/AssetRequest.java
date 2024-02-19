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
package org.eclipse.tractusx.irs.edc.client.asset.model;

import lombok.Builder;
import org.eclipse.edc.spi.types.domain.DataAddress;
import org.eclipse.edc.spi.types.domain.asset.Asset;

/**
 * AssetRequest used for creating edc notification asset
 */

@Builder(toBuilder = true)
public record AssetRequest(
        Asset asset,
        DataAddress dataAddress) {
    public static final String ASSET_CREATION_ASSET = "https://w3id.org/edc/v0.0.1/ns/asset";
    public static final String ASSET_CREATION_DATA_ADDRESS = "https://w3id.org/edc/v0.0.1/ns/dataAddress";
    public static final String ASSET_CREATION_DATA_ADDRESS_TYPE = "https://w3id.org/edc/v0.0.1/ns/dataAddress/type";
    public static final String ASSET_CREATION_DATA_ADDRESS_BASE_URL = "https://w3id.org/edc/v0.0.1/ns/dataAddress/baseUrl";
    public static final String ASSET_CREATION_DATA_ADDRESS_PROXY_METHOD = "https://w3id.org/edc/v0.0.1/ns/dataAddress/proxyMethod";
    public static final String ASSET_CREATION_DATA_ADDRESS_PROXY_BODY = "https://w3id.org/edc/v0.0.1/ns/dataAddress/proxyBody";
    public static final String ASSET_CREATION_DATA_ADDRESS_PROXY_PATH = "https://w3id.org/edc/v0.0.1/ns/dataAddress/proxyPath";
    public static final String ASSET_CREATION_DATA_ADDRESS_PROXY_QUERY_PARAMS = "https://w3id.org/edc/v0.0.1/ns/dataAddress/proxyQueryParams";
    public static final String ASSET_CREATION_DATA_ADDRESS_METHOD = "https://w3id.org/edc/v0.0.1/ns/dataAddress/method";
}
