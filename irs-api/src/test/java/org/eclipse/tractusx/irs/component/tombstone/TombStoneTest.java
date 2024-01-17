/********************************************************************************
 * Copyright (c) 2021,2022,2023
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2023: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
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
package org.eclipse.tractusx.irs.component.tombstone;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import io.github.resilience4j.retry.RetryRegistry;
import org.eclipse.tractusx.irs.component.ProcessingError;
import org.eclipse.tractusx.irs.component.Tombstone;
import org.eclipse.tractusx.irs.component.enums.ProcessStep;
import org.junit.jupiter.api.Test;

class TombStoneTest {

    Tombstone tombstone;

    @Test
    void fromTombstoneTest() {
        // arrange
        String catenaXId = "5e3e9060-ba73-4d5d-a6c8-dfd5123f4d99";
        IllegalArgumentException illegalArgumentException = new IllegalArgumentException("Some funny error occur");
        String endPointUrl = "http://localhost/dummy/interfaceinformation/urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6";

        ProcessingError processingError = ProcessingError.builder()
                                                         .withProcessStep(ProcessStep.SUBMODEL_REQUEST)
                                                         .withRetryCounter(RetryRegistry.ofDefaults()
                                                                                        .getDefaultConfig()
                                                                                        .getMaxAttempts())
                                                         .withLastAttempt(ZonedDateTime.now(ZoneOffset.UTC))
                                                         .withErrorDetail("Some funny error occur")
                                                         .build();

        Tombstone expectedTombstone = Tombstone.builder()
                                               .catenaXId(catenaXId)
                                               .endpointURL(endPointUrl)
                                               .processingError(processingError)
                                               .build();

        //act
        tombstone = Tombstone.from(catenaXId, endPointUrl, illegalArgumentException,
                RetryRegistry.ofDefaults().getDefaultConfig().getMaxAttempts(), ProcessStep.SUBMODEL_REQUEST);

        // assert
        assertThat(tombstone).isNotNull();
        assertThat(tombstone.getProcessingError().getErrorDetail()).isEqualTo(processingError.getErrorDetail());
        assertThat(tombstone.getProcessingError().getRetryCounter()).isEqualTo(processingError.getRetryCounter());
        assertThat(zonedDateTimeExcerpt(tombstone.getProcessingError().getLastAttempt())).isEqualTo(
                zonedDateTimeExcerpt(processingError.getLastAttempt()));
        assertThat(tombstone.getCatenaXId()).isEqualTo(expectedTombstone.getCatenaXId());
        assertThat(tombstone.getEndpointURL()).isEqualTo(expectedTombstone.getEndpointURL());
        assertThat(tombstone.getProcessingError().getRetryCounter()).isEqualTo(
                expectedTombstone.getProcessingError().getRetryCounter());
    }

    private String zonedDateTimeExcerpt(ZonedDateTime dateTime) {
        return new StringBuilder().append(dateTime.getYear())
                                  .append("-")
                                  .append(dateTime.getMonth())
                                  .append("-")
                                  .append(dateTime.getDayOfMonth())
                                  .append("T")
                                  .append(dateTime.getHour())
                                  .append(":")
                                  .append(dateTime.getMinute())
                                  .append(":")
                                  .append(dateTime.getSecond())
                                  .toString();
    }

}
