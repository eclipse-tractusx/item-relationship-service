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
package org.eclipse.tractusx.irs.testing.containers;

import java.time.Duration;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.utility.Base58;

/**
 * Testcontainer for MinIO
 */
public class MinioContainer extends GenericContainer<MinioContainer> {

    private static final int DEFAULT_PORT = 9000;
    private static final String DEFAULT_IMAGE = "minio/minio";
    private static final String DEFAULT_TAG = "edge";

    private static final String MINIO_ACCESS_KEY = "MINIO_ACCESS_KEY";
    private static final String MINIO_SECRET_KEY = "MINIO_SECRET_KEY";

    private static final String DEFAULT_STORAGE_DIRECTORY = "/data";
    private static final String HEALTH_ENDPOINT = "/minio/health/ready";
    private static final int CONTAINER_ID_LENGTH = 6;

    public MinioContainer(final CredentialsProvider credentials) {
        this(DEFAULT_IMAGE + ":" + DEFAULT_TAG, credentials);
    }

    public MinioContainer(final String image, final CredentialsProvider credentials) {
        super(image == null ? DEFAULT_IMAGE + ":" + DEFAULT_TAG : image);
        withNetworkAliases("minio-" + Base58.randomString(CONTAINER_ID_LENGTH));
        addExposedPort(DEFAULT_PORT);
        if (credentials != null) {
            withEnv(MINIO_ACCESS_KEY, credentials.getAccessKey());
            withEnv(MINIO_SECRET_KEY, credentials.getSecretKey());
        }
        withCommand("server", DEFAULT_STORAGE_DIRECTORY);
        withMinimumRunningDuration(Duration.ofSeconds(2));
        setWaitStrategy(new HttpWaitStrategy().forPort(DEFAULT_PORT)
                                              .forPath(HEALTH_ENDPOINT)
                                              .withStartupTimeout(Duration.ofMinutes(2)));
    }

    public String getHostAddress() {
        return getHost() + ":" + getMappedPort(DEFAULT_PORT);
    }

    /**
     * Credential provider for MinIO
     */
    public static class CredentialsProvider {
        private final String accessKey;
        private final String secretKey;

        public CredentialsProvider(final String accessKey, final String secretKey) {
            this.accessKey = accessKey;
            this.secretKey = secretKey;
        }

        public String getAccessKey() {
            return accessKey;
        }

        public String getSecretKey() {
            return secretKey;
        }
    }
}
