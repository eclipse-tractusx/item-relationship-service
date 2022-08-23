//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.bpdm;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

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
        final BusinessPartnerResponse businessPartner = bpdmClient.getBusinessPartner(manufacturerId, BPN_TYPE);

        final List<NameResponse> names = businessPartner.getNames();
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
