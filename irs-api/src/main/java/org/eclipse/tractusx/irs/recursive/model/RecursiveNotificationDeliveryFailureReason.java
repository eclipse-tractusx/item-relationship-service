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
package org.eclipse.tractusx.irs.recursive.model;

/** Local classification of a failed recursive EDC notification delivery. */
public enum RecursiveNotificationDeliveryFailureReason {
    NO_CONNECTOR_ENDPOINT,
    CONNECTOR_DISCOVERY_FAILED,
    CONNECTOR_ENDPOINT_INVALID,
    NOTIFICATION_ASSET_NOT_FOUND,
    NOTIFICATION_POLICY_REJECTED,
    CATALOG_REQUEST_FAILED,
    CONTRACT_NEGOTIATION_FAILED,
    DATA_PLANE_DELIVERY_FAILED,
    EDC_NOTIFICATION_FAILED
}
