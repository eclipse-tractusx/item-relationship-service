/********************************************************************************
 * Copyright (c) 2026 Volkswagen AG
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
package org.eclipse.tractusx.irs.recursive.service;

import org.eclipse.tractusx.irs.recursive.model.RecursiveNotificationDeliveryFailureReason;

/**
 * A recursive notification could not be delivered to the DIRECT partner.
 *
 * <p>The reason classifies the local delivery step that failed (e.g. NO_CONNECTOR_ENDPOINT,
 * NOTIFICATION_ASSET_NOT_FOUND, NOTIFICATION_ASSET_AMBIGUOUS, CONTRACT_NEGOTIATION_FAILED,
 * DATA_PLANE_DELIVERY_FAILED).
 * This detail is allowed locally - the direct partner is known from the own BOM anyway - and is
 * persisted on the job plus logged with the errorRef. It never travels to the parent: more distant
 * chain members only see the coarse CHILD_BRANCH_FAILED tombstone carrying the same
 * errorRef, so diagnosis runs hop by hop.</p>
 */
class RecursiveNotificationDeliveryException extends RuntimeException {

    private final RecursiveNotificationDeliveryFailureReason reason;
    private final String errorRef;

    /* package */ RecursiveNotificationDeliveryException(
            final RecursiveNotificationDeliveryFailureReason reason, final String errorRef, final String message,
            final Throwable cause) {
        super(message, cause);
        this.reason = reason;
        this.errorRef = errorRef;
    }

    /* package */ RecursiveNotificationDeliveryFailureReason getReason() {
        return reason;
    }

    /* package */ String getErrorRef() {
        return errorRef;
    }
}
