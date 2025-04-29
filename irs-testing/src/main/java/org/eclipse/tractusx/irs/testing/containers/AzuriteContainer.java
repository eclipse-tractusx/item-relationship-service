/********************************************************************************
 * Copyright (c) 2022,2024
 *       2022: ZF Friedrichshafen AG
 *       2022: ISTOS GmbH
 *       2022,2024: Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *       2022,2023: BOSCH AG
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
package org.eclipse.tractusx.irs.testing.containers;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Testcontainer for Azurite (Azure blob storage)
 */
public class AzuriteContainer extends GenericContainer<AzuriteContainer> {

    private static final int BLOB_PORT = 10_000;
    private static final int QUEUE_PORT = 10_001;
    private static final int TABLE_PORT = 10_002;
    @SuppressWarnings("PMD.AvoidUsingHardCodedIP")
    private static final String IP_ADDRESS = "0.0.0.0";
    private static final DockerImageName AZURITE_IMAGE = DockerImageName.parse("mcr.microsoft.com/azure-storage/azurite");
    private static final String ACCOUNT_NAME = "devstoreaccount1";
    private static final String ACCOUNT_KEY = "Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==";

    public AzuriteContainer() {
        super(AZURITE_IMAGE);
        withExposedPorts(BLOB_PORT, QUEUE_PORT, TABLE_PORT);
        withCommand("azurite", "--loose", "--blobHost", IP_ADDRESS, "--queueHost", IP_ADDRESS, "--tableHost",
                IP_ADDRESS);
    }

    public String getBlobEndpoint() {
        return String.format("http://%s:%d", getHost(), getMappedPort(BLOB_PORT));
    }

    public String getQueueEndpoint() {
        return String.format("http://%s:%d", getHost(), getMappedPort(QUEUE_PORT));
    }

    public String getTableEndpoint() {
        return String.format("http://%s:%d", getHost(), getMappedPort(TABLE_PORT));
    }

    public String getConnectionString() {
        return String.format(
                "DefaultEndpointsProtocol=http;"
                + "AccountName=%s;"
                + "AccountKey=%s;"
                + "BlobEndpoint=%s/devstoreaccount1;"
                + "QueueEndpoint=%s/devstoreaccount1;"
                + "TableEndpoint=%s/devstoreaccount1;",
                ACCOUNT_NAME,
                ACCOUNT_KEY,
                getBlobEndpoint(),
                getQueueEndpoint(),
                getTableEndpoint()
        );
    }
}
