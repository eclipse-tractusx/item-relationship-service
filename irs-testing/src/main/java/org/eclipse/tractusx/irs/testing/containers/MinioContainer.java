/********************************************************************************
 * Copyright (c) 2022 ZF Friedrichshafen AG
 * Copyright (c) 2022 ISTOS GmbH
 * Copyright (c) 2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 * Copyright (c) 2023 BOSCH AG
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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
package org.eclipse.tractusx.irs.testing.containers;

import java.time.Duration;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.utility.Base58;

/**
 * SeaweedFS-backed S3 test container without credential validation.
 */
public class MinioContainer extends GenericContainer<MinioContainer> {

    private static final int DEFAULT_PORT = 8333;
    private static final String DEFAULT_IMAGE = "chrislusf/seaweedfs";
    private static final String DEFAULT_TAG = "4.48";

    private static final String DEFAULT_STORAGE_DIRECTORY = "/data";
    /** Keeps the sparse volume files small. */
    private static final String VOLUME_SIZE_LIMIT_MB = "64";
    /**
     * Unlimited volume count. A small shared limit can prevent additional test buckets from allocating volumes.
     */
    private static final String MAX_VOLUMES = "0";

    private static final String READY_ENDPOINT = "/";
    private static final int READY_STATUS_CODE = 200;
    private static final int CONTAINER_ID_LENGTH = 6;

    public MinioContainer(final CredentialsProvider credentials) {
        this(DEFAULT_IMAGE + ":" + DEFAULT_TAG, credentials);
    }

    @SuppressWarnings("PMD.UnusedFormalParameter")
    public MinioContainer(final String image, final CredentialsProvider credentials) {
        super(image == null ? DEFAULT_IMAGE + ":" + DEFAULT_TAG : image);
        withNetworkAliases("blobstore-" + Base58.randomString(CONTAINER_ID_LENGTH));
        addExposedPort(DEFAULT_PORT);
        withCommand("server", "-s3", "-dir=" + DEFAULT_STORAGE_DIRECTORY,
                "-master.volumeSizeLimitMB=" + VOLUME_SIZE_LIMIT_MB, "-volume.max=" + MAX_VOLUMES);
        withMinimumRunningDuration(Duration.ofSeconds(2));
        setWaitStrategy(new HttpWaitStrategy().forPort(DEFAULT_PORT)
                                              .forPath(READY_ENDPOINT)
                                              .forStatusCode(READY_STATUS_CODE)
                                              .withStartupTimeout(Duration.ofMinutes(2)));
    }

    public String getHostAddress() {
        return getHost() + ":" + getMappedPort(DEFAULT_PORT);
    }

    /**
     * Credentials used by the tests to connect to the blob store.
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
