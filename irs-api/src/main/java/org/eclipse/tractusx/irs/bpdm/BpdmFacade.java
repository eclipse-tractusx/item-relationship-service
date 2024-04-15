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
package org.eclipse.tractusx.irs.bpdm;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

/**
 * Public API Facade for bpdm domain
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BpdmFacade {

    private static final String BPDM_CACHE_NAME = "bpdm_cache";
    private static final String BPN_TYPE = "BPN";

    private final BpdmClient bpdmClient;

    @Cacheable(value = BPDM_CACHE_NAME, key = "#manufacturerId")
    public Optional<String> findManufacturerName(final String manufacturerId) {

        final List<NameResponse> names;
        try {
            final BusinessPartnerResponse businessPartner = bpdmClient.getBusinessPartner(manufacturerId, BPN_TYPE);
            names = businessPartner.getNames();
        } catch (final RestClientException e) {
            log.warn(e.getMessage(), e);
            return Optional.empty();
        }

        if (names.isEmpty()) {
            log.warn("Names not found for {} BPN", manufacturerId);
            return Optional.empty();
        }

        return names.stream()
                    .filter(it -> StringUtils.isNotBlank(it.getValue()))
                    .findFirst()
                    .map(NameResponse::getValue);
    }

}
