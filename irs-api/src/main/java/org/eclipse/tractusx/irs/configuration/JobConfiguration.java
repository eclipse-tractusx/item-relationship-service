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
package org.eclipse.tractusx.irs.configuration;

import java.time.Clock;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import io.github.resilience4j.retry.RetryRegistry;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.eclipse.tractusx.irs.aaswrapper.job.AASRecursiveJobHandler;
import org.eclipse.tractusx.irs.aaswrapper.job.AASTransferProcess;
import org.eclipse.tractusx.irs.aaswrapper.job.AASTransferProcessManager;
import org.eclipse.tractusx.irs.aaswrapper.job.ItemDataRequest;
import org.eclipse.tractusx.irs.aaswrapper.job.ItemTreesAssembler;
import org.eclipse.tractusx.irs.aaswrapper.job.TreeRecursiveLogic;
import org.eclipse.tractusx.irs.aaswrapper.job.delegate.DigitalTwinDelegate;
import org.eclipse.tractusx.irs.aaswrapper.job.delegate.RelationshipDelegate;
import org.eclipse.tractusx.irs.aaswrapper.job.delegate.SubmodelDelegate;
import org.eclipse.tractusx.irs.common.OutboundMeterRegistryService;
import org.eclipse.tractusx.irs.common.persistence.AzureBlobPersistence;
import org.eclipse.tractusx.irs.common.persistence.BlobPersistence;
import org.eclipse.tractusx.irs.common.persistence.BlobPersistenceException;
import org.eclipse.tractusx.irs.common.persistence.MinioBlobPersistence;
import org.eclipse.tractusx.irs.connector.job.JobOrchestrator;
import org.eclipse.tractusx.irs.connector.job.JobStore;
import org.eclipse.tractusx.irs.connector.job.JobTTL;
import org.eclipse.tractusx.irs.data.CxTestDataContainer;
import org.eclipse.tractusx.irs.edc.client.EdcConfiguration;
import org.eclipse.tractusx.irs.edc.client.EdcDataPlaneClient;
import org.eclipse.tractusx.irs.edc.client.EdcOrchestrator;
import org.eclipse.tractusx.irs.edc.client.EdcSubmodelClient;
import org.eclipse.tractusx.irs.edc.client.EdcSubmodelClientImpl;
import org.eclipse.tractusx.irs.edc.client.EdcSubmodelClientLocalStub;
import org.eclipse.tractusx.irs.edc.client.EdcSubmodelFacade;
import org.eclipse.tractusx.irs.common.persistence.config.AzureBlobstoreConfiguration;
import org.eclipse.tractusx.irs.common.persistence.config.BlobStoreConfiguration;
import org.eclipse.tractusx.irs.common.persistence.config.BlobStoreContainerConfiguration;
import org.eclipse.tractusx.irs.common.persistence.config.MinioBlobstoreConfiguration;
import org.eclipse.tractusx.irs.registryclient.DigitalTwinRegistryService;
import org.eclipse.tractusx.irs.registryclient.central.DigitalTwinRegistryClient;
import org.eclipse.tractusx.irs.registryclient.central.DigitalTwinRegistryClientLocalStub;
import org.eclipse.tractusx.irs.registryclient.discovery.ConnectorEndpointsService;
import org.eclipse.tractusx.irs.semanticshub.SemanticsHubFacade;
import org.eclipse.tractusx.irs.services.MeterRegistryService;
import org.eclipse.tractusx.irs.services.validation.JsonValidatorService;
import org.eclipse.tractusx.irs.util.JsonUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Spring configuration for job-related beans.
 */
@Configuration
@SuppressWarnings({ "PMD.ExcessiveImports",
                    "PMD.TooManyMethods"
})
public class JobConfiguration {
    public static final String JOB_BLOB_PERSISTENCE = "jobPersistence";

    @Bean
    public OutboundMeterRegistryService outboundMeterRegistryService(final MeterRegistry meterRegistry,
            final RetryRegistry retryRegistry) {
        return new OutboundMeterRegistryService(meterRegistry, retryRegistry);
    }

    @Bean
    public JobOrchestrator<ItemDataRequest, AASTransferProcess> jobOrchestrator(
            final DigitalTwinDelegate digitalTwinDelegate,
            @Qualifier(JOB_BLOB_PERSISTENCE) final BlobPersistence blobStore,
            final JobStore jobStore,
            final MeterRegistryService meterService,
            final ApplicationEventPublisher applicationEventPublisher,
            @Value("${irs.job.jobstore.ttl.failed:}") final Duration ttlFailedJobs,
            @Value("${irs.job.jobstore.ttl.completed:}") final Duration ttlCompletedJobs,
            final JsonUtil jsonUtil,
            @Value("${irs.job.cached.threadCount}") final int threadCount) {

        final var manager = new AASTransferProcessManager(digitalTwinDelegate, cachedExecutorService(threadCount),
                blobStore, jsonUtil);
        final var logic = new TreeRecursiveLogic(blobStore, jsonUtil, new ItemTreesAssembler());
        final var handler = new AASRecursiveJobHandler(logic);
        final JobTTL jobTTL = new JobTTL(ttlCompletedJobs, ttlFailedJobs);

        return new JobOrchestrator<>(manager, jobStore, handler, meterService, applicationEventPublisher, jobTTL);
    }

    @Bean
    public ExecutorService cachedExecutorService(@Value("${irs.job.cached.threadCount}") final int threadCount) {
        final long keepAliveTime = 60L;

        return new ThreadPoolExecutor(
                threadCount,
                threadCount,
                keepAliveTime, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>());
    }

    @Bean
    public ScheduledExecutorService scheduledExecutorService(@Value("${irs.job.scheduled.threadCount}") final int threadCount) {
        return Executors.newScheduledThreadPool(threadCount);
    }

    @Bean
    public ExecutorService fixedThreadPoolExecutorService(@Value("${irs-edc-client.controlplane.orchestration.thread-pool-size:}")  final int threadPoolSize) {
        return Executors.newFixedThreadPool(threadPoolSize);
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Profile("!test")
    @Bean(JOB_BLOB_PERSISTENCE)
    @ConditionalOnProperty(name = "blobstore.persistence.storeType", havingValue = "MINIO")
    public BlobPersistence minioBlobStore(final BlobStoreConfiguration config) throws BlobPersistenceException {
        final MinioBlobstoreConfiguration minioConfig = config.getPersistence().getMinio();
        final BlobStoreContainerConfiguration jobsConfig = config.getJobs();

        if (minioConfig == null || jobsConfig == null) {
            throw new IllegalArgumentException("Missing blob storage configuration");
        }

        return new MinioBlobPersistence(minioConfig.getEndpoint(), minioConfig.getAccessKey(), minioConfig.getSecretKey(),
                jobsConfig.getContainerName(), jobsConfig.getDaysToLive());
    }

    @Profile("!test")
    @Bean(JOB_BLOB_PERSISTENCE)
    @ConditionalOnProperty(name = "blobstore.persistence.storeType", havingValue = "AZURE")
    public BlobPersistence azureBlobStore(final BlobStoreConfiguration config) {
        final AzureBlobstoreConfiguration azureConfig = config.getPersistence().getAzure();
        final BlobStoreContainerConfiguration jobsConfig = config.getJobs();

        if (azureConfig == null || jobsConfig == null) {
            throw new IllegalArgumentException("Missing blob storage configuration");
        }

        if (azureConfig.isUseConnectionString()) {
            return new AzureBlobPersistence(azureConfig.getConnectionString(), jobsConfig.getContainerName());
        } else {
            return new AzureBlobPersistence(azureConfig.getBaseUrl(), azureConfig.getClientId(), azureConfig.getClientSecret(),
                    azureConfig.getTenantId(), jobsConfig.getContainerName());
        }
    }

    @Bean
    public JsonUtil jsonUtil() {
        return new JsonUtil();
    }

    @Bean
    public TimedAspect timedAspect(final MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    @Bean
    public DigitalTwinDelegate digitalTwinDelegate(final RelationshipDelegate relationshipDelegate,
            final DigitalTwinRegistryService digitalTwinRegistryService) {
        return new DigitalTwinDelegate(relationshipDelegate, digitalTwinRegistryService);
    }

    @Bean
    public RelationshipDelegate relationshipDelegate(final SubmodelDelegate submodelDelegate,
            final EdcSubmodelFacade submodelFacade, final ConnectorEndpointsService connectorEndpointsService,
            final JsonUtil jsonUtil) {
        return new RelationshipDelegate(submodelDelegate, submodelFacade, connectorEndpointsService, jsonUtil);
    }

    @Bean
    public SubmodelDelegate submodelDelegate(final EdcSubmodelFacade submodelFacade,
            final SemanticsHubFacade semanticsHubFacade, final JsonValidatorService jsonValidatorService,
            final ConnectorEndpointsService connectorEndpointsService) {
        return new SubmodelDelegate(submodelFacade, semanticsHubFacade, jsonValidatorService, jsonUtil(),
                connectorEndpointsService);
    }

    @Profile({ "local",
               "stubtest"
    })
    @Bean
    public DigitalTwinRegistryClient digitalTwinRegistryClient(final CxTestDataContainer cxTestDataContainer) {
        return new DigitalTwinRegistryClientLocalStub(cxTestDataContainer);
    }

    @Profile({ "local",
               "stubtest"
    })
    @Bean
    public EdcSubmodelClient edcLocalSubmodelClient(final CxTestDataContainer cxTestDataContainer) {
        return new EdcSubmodelClientLocalStub(cxTestDataContainer);
    }

    @Profile({ "!local && !stubtest" })
    @Bean
    public EdcSubmodelClient edcSubmodelClient(final EdcConfiguration edcConfiguration, final EdcDataPlaneClient edcDataPlaneClient,
            final EdcOrchestrator edcOrchestrator, final RetryRegistry retryRegistry) {
        return new EdcSubmodelClientImpl(edcConfiguration, edcDataPlaneClient, edcOrchestrator, retryRegistry);
    }
}
