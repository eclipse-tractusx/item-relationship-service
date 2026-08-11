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
package org.eclipse.tractusx.irs.recursive.e2e.testcontainers;

import org.eclipse.tractusx.irs.testing.containers.MinioContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Isolated MinIO and Redis containers for one recursive IRS partner.
 */
public final class RecursivePartnerContainers implements AutoCloseable {

    private static final String ACCESS_KEY = "minio";
    private static final String SECRET_KEY = "minio123";
    private static final DockerImageName REDIS_IMAGE = DockerImageName.parse("redis:7.4.2");
    public static final int REDIS_PORT = 6379;
    public static final int MINIO_CONSOLE_PORT = 9001;

    private final String partnerId;
    private final MinioContainer minio;
    private final GenericContainer<?> redis;

    private RecursivePartnerContainers(final String partnerId) {
        this.partnerId = partnerId;
        minio = new MinioContainer(new MinioContainer.CredentialsProvider(ACCESS_KEY, SECRET_KEY));
        minio.withEnv("MINIO_ROOT_USER", ACCESS_KEY);
        minio.withEnv("MINIO_ROOT_PASSWORD", SECRET_KEY);
        minio.withCommand("server", "/data", "--console-address", ":" + MINIO_CONSOLE_PORT);
        minio.addExposedPort(MINIO_CONSOLE_PORT);
        redis = new GenericContainer<>(REDIS_IMAGE).withExposedPorts(REDIS_PORT);
    }

    public static RecursivePartnerContainers containersFor(final String partnerId) {
        return new RecursivePartnerContainers(partnerId);
    }

    public void start() {
        minio.start();
        redis.start();
    }

    @Override
    public void close() {
        redis.stop();
        minio.stop();
    }

    public String partnerId() {
        return partnerId;
    }

    public String minioEndpoint() {
        return "http://" + minio.getHostAddress();
    }

    public String minioConsoleEndpoint() {
        return "http://" + minio.getHost() + ":" + minio.getMappedPort(MINIO_CONSOLE_PORT);
    }

    public String minioAccessKey() {
        return ACCESS_KEY;
    }

    public String minioSecretKey() {
        return SECRET_KEY;
    }

    public String redisHost() {
        return redis.getHost();
    }

    public int redisPort() {
        return redis.getMappedPort(REDIS_PORT);
    }

    public String bucketSuffix() {
        return partnerId.replaceAll("[^a-zA-Z0-9-]", "-");
    }

}
