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
package org.eclipse.tractusx.irs.recursive.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.eclipse.tractusx.irs.recursive.model.RecursiveErrorCode;
import org.eclipse.tractusx.irs.recursive.model.RecursiveErrorResponse;
import org.eclipse.tractusx.irs.recursive.model.RecursiveNotificationResponse;
import org.eclipse.tractusx.irs.recursive.service.RecursiveNotificationAuthenticationException;
import org.eclipse.tractusx.irs.recursive.service.RecursiveNotificationReceiver;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class RecursiveNotificationControllerTest {

    private static final String PARENT_BPNL = "BPNL0000PARENT01";

    private final RecursiveNotificationReceiver notificationReceiver = mock(RecursiveNotificationReceiver.class);
    private final RecursiveNotificationController controller =
            new RecursiveNotificationController(notificationReceiver);
    private final RecursiveExceptionHandler exceptionHandler = new RecursiveExceptionHandler();

    @Test
    void delegatesNotificationToReceiver() {
        final JsonNode payload = new ObjectMapper().createObjectNode();
        final RecursiveNotificationResponse notificationResponse = RecursiveNotificationResponse.builder()
                .status("accepted")
                .messageId("68904173-ad59-4a77-8412-3e73fcafbd8b")
                .build();
        when(notificationReceiver.receive(PARENT_BPNL, payload)).thenReturn(notificationResponse);

        final ResponseEntity<RecursiveNotificationResponse> response =
                controller.receiveNotification(PARENT_BPNL, payload);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(notificationResponse);
        verify(notificationReceiver).receive(PARENT_BPNL, payload);
    }

    @Test
    void authenticationFailureMapsToForbidden() {
        final ResponseEntity<RecursiveErrorResponse> response = exceptionHandler.handleNotificationAuthentication(
                new RecursiveNotificationAuthenticationException("mismatch"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().getCode())
                .isEqualTo(RecursiveErrorCode.NOTIFICATION_AUTHENTICATION_FAILED);
    }
}
