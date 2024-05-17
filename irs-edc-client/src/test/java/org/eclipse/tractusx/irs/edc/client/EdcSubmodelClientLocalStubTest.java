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
package org.eclipse.tractusx.irs.edc.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.tractusx.irs.edc.client.exceptions.EdcClientException;
import org.eclipse.tractusx.irs.edc.client.model.notification.EdcNotification;
import org.eclipse.tractusx.irs.edc.client.model.notification.NotificationContent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EdcSubmodelClientLocalStubTest {

    @InjectMocks
    private EdcSubmodelClientLocalStub edcSubmodelClientLocalStub;

    @Test
    void shouldThrowExceptionFor() {
        // given
        String assetId = "urn:uuid:c35ee875-5443-4a2d-bc14-fdacd64b9446";

        // when
        assertThrows(EdcClientException.class,
                () -> edcSubmodelClientLocalStub.getSubmodelPayload("", "", assetId, ""));
    }

    @Test
    void sendNotification() {
        final EdcNotification<NotificationContent> notification = EdcNotification.builder().build();
        final var actual = edcSubmodelClientLocalStub.sendNotification("", "", notification, "");
        assertThat(actual).isCompleted();
    }

    @Test
    void getEndpointReferencesForAsset() {
        assertThrows(EdcClientException.class,
                () -> edcSubmodelClientLocalStub.getEndpointReferencesForAsset("", "", "", ""));
    }

    @Test
    void testGetEndpointReferencesForAsset() {
        assertThrows(EdcClientException.class,
                () -> edcSubmodelClientLocalStub.getEndpointReferencesForAsset("", "", "", ""));
    }

    @Test
    void getEndpointReferencesForRegistryAsset() {
        assertThrows(EdcClientException.class,
                () -> edcSubmodelClientLocalStub.getEndpointReferencesForRegistryAsset("", ""));
    }
}