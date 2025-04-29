/********************************************************************************
 * Copyright (c) 2021,2025 Contributors to the Eclipse Foundation
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

import org.eclipse.tractusx.irs.testing.containers.AzuriteContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AzuriteContainerTest {

    private static AzuriteContainer azurite;

    @BeforeAll
    static void startContainer() {
        azurite = new AzuriteContainer();
        azurite.start();
    }

    @AfterAll
    static void stopContainer() {
        if (azurite != null) {
            azurite.stop();
        }
    }

    @Test
    void shouldExposeBlobEndpoint() {
        String endpoint = azurite.getBlobEndpoint();
        assertNotNull(endpoint);
        assertTrue(endpoint.startsWith("http://"));
    }

    @Test
    void shouldExposeQueueEndpoint() {
        String endpoint = azurite.getQueueEndpoint();
        assertNotNull(endpoint);
        assertTrue(endpoint.startsWith("http://"));
    }

    @Test
    void shouldExposeTableEndpoint() {
        String endpoint = azurite.getTableEndpoint();
        assertNotNull(endpoint);
        assertTrue(endpoint.startsWith("http://"));
    }

    @Test
    void shouldBuildValidConnectionString() {
        String connStr = azurite.getConnectionString();
        assertNotNull(connStr);
        assertTrue(connStr.contains("AccountName=devstoreaccount1"));
        assertTrue(connStr.contains("BlobEndpoint="));
    }
}
