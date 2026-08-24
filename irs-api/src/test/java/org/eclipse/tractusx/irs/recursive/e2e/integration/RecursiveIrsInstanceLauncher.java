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
package org.eclipse.tractusx.irs.recursive.e2e.integration;

import org.eclipse.tractusx.irs.IrsApplication;
import org.eclipse.tractusx.irs.recursive.e2e.testcontainers.RecursivePartnerContainers;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Starts a recursive IRS instance without replacing recursive business beans.
 */
public final class RecursiveIrsInstanceLauncher {

    private RecursiveIrsInstanceLauncher() {
    }

    public static RecursiveIrsInstance start(final String id, final String bpnl,
            final RecursivePartnerContainers containers, final RecursiveExternalSystems externalSystems) {
        final ConfigurableApplicationContext context = new SpringApplicationBuilder(
                IrsApplication.class, RecursiveIntegrationRestTemplateConfiguration.class)
                .web(WebApplicationType.SERVLET)
                .profiles("integrationtest")
                .run(commandLineProperties(id, bpnl, containers, externalSystems));
        final int port = ((ServletWebServerApplicationContext) context).getWebServer().getPort();
        return new RecursiveIrsInstance(id, bpnl, containers, context, "http://localhost:" + port);
    }

    private static String[] commandLineProperties(final String id, final String bpnl,
            final RecursivePartnerContainers containers, final RecursiveExternalSystems externalSystems) {
        final String bucketSuffix = containers.bucketSuffix();
        return new String[] {
                "--spring.main.banner-mode=off",
                "--server.port=0",
                "--management.server.port=0",
                "--irs.recursive.localBpnl=" + bpnl,
                "--irs.security.api.keys.admin=" + RecursiveIntegrationTestClient.ADMIN_API_KEY,
                "--irs.security.api.keys.regular=recursive-regular-api-key",
                "--irs.job.cached.threadCount=2",
                "--irs.job.scheduled.threadCount=5",
                "--irs.job.recursive.threadCount=4",
                "--irs.recursive.timeout.timeoutCheckInterval=PT1S",
                // Several full IRS instances share one machine during these tests. A small
                // orchestrator pool and a tight async timeout produce spurious
                // "Timeout while getting submodel payload" failures under load.
                "--irs-edc-client.controlplane.orchestration.thread-pool-size=8",
                "--irs-edc-client.controlplane.datareference.storage.useRedis=true",
                "--irs-edc-client.controlplane.endpoint.data=" + externalSystems.baseUrl(),
                "--irs-edc-client.controlplane.endpoint.catalog=/catalog/request",
                "--irs-edc-client.controlplane.endpoint.contract-negotiation=/contractnegotiations",
                "--irs-edc-client.controlplane.endpoint.transfer-process=/transferprocesses",
                "--irs-edc-client.controlplane.endpoint.state-suffix=/state",
                "--irs-edc-client.controlplane.provider-suffix=/api/v1/dsp",
                "--irs-edc-client.controlplane.api-key.header=X-API-KEY",
                "--irs-edc-client.controlplane.api-key.secret=recursive-edc-api-key",
                "--irs-edc-client.async-timeout=PT2M",
                "--resilience4j.retry.configs.default.maxAttempts=1",
                "--spring.data.redis.host=" + containers.redisHost(),
                "--spring.data.redis.port=" + containers.redisPort(),
                "--spring.data.redis.password=",
                "--blobstore.persistence.storeType=MINIO",
                "--blobstore.persistence.minio.endpoint=" + containers.minioEndpoint(),
                "--blobstore.persistence.minio.accessKey=" + containers.minioAccessKey(),
                "--blobstore.persistence.minio.secretKey=" + containers.minioSecretKey(),
                "--blobstore.jobs.containerName=irs-ri-jobs-" + bucketSuffix,
                "--blobstore.jobs.daysToLive=1",
                "--blobstore.policies.containerName=irs-ri-policies-" + bucketSuffix,
                "--blobstore.policies.daysToLive=1",
                "--blobstore.chainOpeningGrants.containerName=irs-ri-grants-" + bucketSuffix,
                "--blobstore.chainOpeningGrants.daysToLive=1",
                "--digitalTwinRegistry.type=central",
                "--digitalTwinRegistry.descriptorEndpoint=" + externalSystems.descriptorEndpoint(),
                "--digitalTwinRegistry.shellLookupEndpoint=" + externalSystems.shellLookupEndpoint(),
                "--digitalTwinRegistry.discovery.discoveryFinderUrl=" + externalSystems.discoveryFinderUrl(),
                "--digitalTwinRegistry.discovery.type=bpn",
                "--semanticshub.url=" + externalSystems.semanticHubUrl(),
                "--semanticshub.modelJsonSchemaEndpoint=" + externalSystems.semanticHubSchemaUrl(),
                "--semanticshub.defaultUrns="
        };
    }
}
