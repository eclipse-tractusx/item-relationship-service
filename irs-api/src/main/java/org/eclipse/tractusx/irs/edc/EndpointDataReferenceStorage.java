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
package org.eclipse.tractusx.irs.edc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import org.eclipse.dataspaceconnector.spi.types.domain.edr.EndpointDataReference;

/**
 * InMemory storage for endpoint data references.
 */
@Service
public class EndpointDataReferenceStorage {

    Map<String, EndpointDataReference> storageMap = null;

    public void put(String contractAgreementId, EndpointDataReference dataReference) {
        if (storageMap == null) {
            storageMap = new ConcurrentHashMap<>();
        }
        storageMap.put(contractAgreementId, dataReference);
    }

    public EndpointDataReference get(String contractAgreementId) {
        if (!storageMap.containsKey(contractAgreementId)) {
            throw new RuntimeException();
        }
        return storageMap.get(contractAgreementId);
    }

}
