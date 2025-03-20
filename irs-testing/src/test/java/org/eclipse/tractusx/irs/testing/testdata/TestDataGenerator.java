/********************************************************************************
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
package org.eclipse.tractusx.irs.testing.testdata;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import net.datafaker.Faker;

public class TestDataGenerator {

    static final Faker FAKER = new Faker();
    static final String SERIAL_PART_URN = "urn:samm:io.catenax.serial_part:3.0.0#SerialPart";
    static final String SINGLE_LEVEL_USAGE_AS_BUILT_URN = "urn:samm:io.catenax.single_level_usage_as_built:3.0.0#SingleLevelUsageAsBuilt";
    static final String SINGLE_LEVEL_BOM_AS_BUILT_URN = "urn:samm:io.catenax.single_level_bom_as_built:3.0.0#SingleLevelBomAsBuilt";
    static final String UNIT_PIECE = "unit:piece";
    static final String MINIMUM_TIMESTAMP = "2020-01-01T00:00:00Z";

    static String randomSerialNumber() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    static String randomPartId() {
        return "PRT-" + randomSerialNumber();
    }

    static String randomPartInstanceId() {
        final Random random = new Random();
        StringBuilder builder = new StringBuilder(24);
        for (int i = 0; i < 24; i++) {
            int digit = random.nextInt(10);
            builder.append(digit);
        }
        return "NO-" + builder;
    }

    static ZonedDateTime randomTimestamp() {
        ZonedDateTime start = ZonedDateTime.parse(MINIMUM_TIMESTAMP);
        ZonedDateTime end = ZonedDateTime.now();
        long startEpochSecond = start.toEpochSecond();
        long endEpochSecond = end.toEpochSecond();

        long randomEpochSecond = ThreadLocalRandom.current().nextLong(startEpochSecond, endEpochSecond);
        int randomNanoOfSecond = ThreadLocalRandom.current().nextInt(0, 1_000) * 1_000_000;
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(randomEpochSecond, randomNanoOfSecond), start.getZone());
    }

    static String randomCountryCode3() {
        return FAKER.country().countryCode3().toUpperCase();
    }

    static String randomManufacturerPartId() {
        return FAKER.code().isbn10(true);
    }

    static String randomUUID() {
        return "urn:uuid:" + UUID.randomUUID();
    }

    static SingleLevelBomAsBuilt.ChildItem createChildItem(final String childCatenaXId, final String childBpnl) {
        final var quantity = SingleLevelBomAsBuilt.Quantity.builder().unit(UNIT_PIECE).value(1).build();
        final ZonedDateTime timestamp = randomTimestamp();
        return SingleLevelBomAsBuilt.ChildItem.builder()
                                              .catenaXId(childCatenaXId)
                                              .quantity(quantity)
                                              .hasAlternatives(false)
                                              .businessPartner(childBpnl)
                                              .createdOn(timestamp)
                                              .lastModifiedOn(timestamp)
                                              .build();
    }

    static SingleLevelUsageAsBuilt.ParentItem createParentItem(final String parentCatenaXId, final String parentBpnl) {
        final ZonedDateTime timestamp = randomTimestamp();
        final var quantity = SingleLevelUsageAsBuilt.Quantity.builder().value(1).unit(UNIT_PIECE).build();
        return SingleLevelUsageAsBuilt.ParentItem.builder()
                                                 .quantity(quantity)
                                                 .isOnlyPotentialParent(false)
                                                 .createdOn(timestamp)
                                                 .lastModifiedOn(timestamp)
                                                 .businessPartner(parentBpnl)
                                                 .catenaXId(parentCatenaXId)
                                                 .build();
    }

    static SingleLevelUsageAsBuilt createSingleLevelUsageAsBuilt(final String catenaXId,
            final List<SingleLevelUsageAsBuilt.ParentItem> parentItems) {
        final List<String> customers = parentItems.stream()
                                                  .map(SingleLevelUsageAsBuilt.ParentItem::getBusinessPartner)
                                                  .toList();
        return new SingleLevelUsageAsBuilt(catenaXId, parentItems, customers);
    }
}
