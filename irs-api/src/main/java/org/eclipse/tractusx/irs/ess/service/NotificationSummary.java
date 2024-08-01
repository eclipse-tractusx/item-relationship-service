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
package org.eclipse.tractusx.irs.ess.service;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.eclipse.tractusx.irs.component.AsyncFetchedItems;
import org.eclipse.tractusx.irs.component.Summary;

/**
 * Extension of the Summary class to also contain notification metrics.
 */
@Jacksonized
@EqualsAndHashCode(callSuper = true)
public class NotificationSummary extends Summary {

    private final NotificationItems notifications;

    public NotificationSummary(final AsyncFetchedItems asyncFetchedItems, final NotificationItems notificationItems) {
        super(asyncFetchedItems);
        this.notifications = notificationItems;
    }

    public NotificationSummary(final AsyncFetchedItems asyncFetchedItems, final Integer sentNotification,
            final Integer receivedNotification) {
        super(asyncFetchedItems);
        this.notifications = new NotificationItems(sentNotification, receivedNotification);
    }
}

/**
 * Describes metrics for notifications.
 */
@Value
class NotificationItems {

    @Schema(description = "Number of sent notifications.", implementation = Integer.class)
    @Min(0) @Max(Integer.MAX_VALUE)
    private Integer sent;

    @Schema(description = "Number of received notification answers.", implementation = Integer.class)
    @Min(0) @Max(Integer.MAX_VALUE)
    private Integer received;

}
