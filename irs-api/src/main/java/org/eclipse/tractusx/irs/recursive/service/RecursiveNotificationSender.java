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

import org.eclipse.tractusx.irs.recursive.model.RecursiveNotificationMessage;

/**
 * Sends recursive notifications to partner IRS instances.
 *
 * <p>Implementations resolve the partner EDC through Discovery and send the
 * message through catalog lookup, policy validation, contract negotiation,
 * transfer and data plane access.</p>
 */
public interface RecursiveNotificationSender {

    /**
     * Sends a REQUEST notification to a child partner IRS.
     *
     * @param receiverBpnl the BPNL of the partner to notify
     * @param message      the notification message to send
     */
    void sendRequest(String receiverBpnl, RecursiveNotificationMessage message);

    /**
     * Sends a RESPONSE notification back to the parent partner IRS.
     *
     * @param receiverBpnl the BPNL of the partner to respond to
     * @param message      the response notification
     */
    void sendResponse(String receiverBpnl, RecursiveNotificationMessage message);
}
