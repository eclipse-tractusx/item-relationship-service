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

import lombok.Getter;

/**
 * Notification used for creating edc notification asset
 */
@Getter
public enum Notification {
    RECEIVE_QUALITY_INVESTIGATION_NOTIFICATION("ReceiveQualityInvestigationNotification", "qualityinvestigationnotification-receive"),
    RECEIVE_QUALITY_ALERT_NOTIFICATION("ReceiveQualityAlertNotification", "qualityalertnotification-receipt"),
    UPDATE_QUALITY_INVESTIGATION_NOTIFICATION("UpdateQualityInvestigationNotification", "qualityinvestigationnotification-update"),
    UPDATE_QUALITY_ALERT_NOTIFICATION("UpdateQualityAlertNotification", "qualityalertnotification-update"),
    RESOLVE_QUALITY_INVESTIGATION_NOTIFICATION("ResolveQualityInvestigationNotification", "qualityinvestigationnotification-resolve"),
    RESOLVE_QUALITY_ALERT_NOTIFICATION("ResolveQualityAlertNotification", "qualityalertnotification-resolve");

    private final String value;

    private final String assetId;

    Notification(final String value, final String assetId) {
        this.value = value;
        this.assetId = assetId;
    }

    public static Notification toNotification(final NotificationMethod notificationMethod,
            final NotificationType notificationType) {
        return switch (notificationType) {
            case QUALITY_ALERT -> switch (notificationMethod) {
                case RECEIVE -> Notification.RECEIVE_QUALITY_ALERT_NOTIFICATION;
                case UPDATE -> Notification.UPDATE_QUALITY_ALERT_NOTIFICATION;
                case RESOLVE -> Notification.RESOLVE_QUALITY_ALERT_NOTIFICATION;
            };
            case QUALITY_INVESTIGATION -> switch (notificationMethod) {
                case RECEIVE -> Notification.RECEIVE_QUALITY_INVESTIGATION_NOTIFICATION;
                case UPDATE -> Notification.UPDATE_QUALITY_INVESTIGATION_NOTIFICATION;
                case RESOLVE -> Notification.RESOLVE_QUALITY_INVESTIGATION_NOTIFICATION;
            };
        };
    }
}
