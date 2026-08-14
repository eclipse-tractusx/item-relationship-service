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
package org.eclipse.tractusx.irs.recursive.config;

import static org.eclipse.tractusx.irs.configuration.JobConfiguration.JOB_BLOB_PERSISTENCE;
import static org.eclipse.tractusx.irs.recursive.config.RecursiveChainOpeningGrantConfiguration.CHAIN_OPENING_GRANT_BLOB_PERSISTENCE;

import java.time.Clock;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jakarta.validation.Validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.tractusx.irs.common.persistence.BlobPersistence;
import org.eclipse.tractusx.irs.edc.client.EdcConfiguration;
import org.eclipse.tractusx.irs.edc.client.EdcDataPlaneClient;
import org.eclipse.tractusx.irs.edc.client.EdcOrchestrator;
import org.eclipse.tractusx.irs.edc.client.EdcSubmodelFacade;
import org.eclipse.tractusx.irs.edc.client.policy.PolicyCheckerService;
import org.eclipse.tractusx.irs.registryclient.DigitalTwinRegistryService;
import org.eclipse.tractusx.irs.registryclient.discovery.ConnectorEndpointsService;
import org.eclipse.tractusx.irs.recursive.service.EdcRecursiveNotificationSender;
import org.eclipse.tractusx.irs.recursive.service.RecursiveChainOpeningGrantService;
import org.eclipse.tractusx.irs.recursive.service.RecursiveJobService;
import org.eclipse.tractusx.irs.recursive.service.RecursiveNotificationMessageValidator;
import org.eclipse.tractusx.irs.recursive.service.RecursiveNotificationReceiver;
import org.eclipse.tractusx.irs.recursive.service.RecursiveNotificationSender;
import org.eclipse.tractusx.irs.recursive.service.RecursiveStartupRecovery;
import org.eclipse.tractusx.irs.recursive.service.RecursiveSubmodelCollector;
import org.eclipse.tractusx.irs.recursive.service.RecursiveTimeoutMonitor;
import org.eclipse.tractusx.irs.recursive.service.RecursiveTraversalService;
import org.eclipse.tractusx.irs.recursive.store.BlobRecursiveChainOpeningGrantStore;
import org.eclipse.tractusx.irs.recursive.store.BlobRecursiveJobStateStore;
import org.eclipse.tractusx.irs.recursive.store.RecursiveChainOpeningGrantStore;
import org.eclipse.tractusx.irs.recursive.store.RecursiveJobStateStore;
import org.eclipse.tractusx.irs.util.JsonUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for the recursive IRS module.
 *
 * <h3>Persistence</h3>
 * <p>Chain opening grants are persisted in their own MinIO / S3 bucket
 * ({@code irs-chain-opening-grants} by default). Recursive job state
 * remains separate in the regular IRS jobs bucket. Each deployed IRS instance
 * must be configured with isolated blob storage or bucket/prefix settings so
 * recursive jobs and grants are not shared across participants.</p>
 *
 * <pre>
 *   irs-chain-opening-grants:
 *     chain-opening-grant:{openingId}:{globalAssetId}:{requesterBpn}:{useCase} = chain opening grant
 *
 *   irs-jobs:
 *     recursive-job:{jobId}                   = recursive job states
 *     recursive-msg:{messageId}               = messageId -> jobId mapping
 * </pre>
 *
 */
@Configuration
@EnableConfigurationProperties(RecursiveProperties.class)
public class RecursiveConfiguration {

    @Bean
    public RecursiveChainOpeningGrantStore recursiveChainOpeningGrantStore(
            @Qualifier(CHAIN_OPENING_GRANT_BLOB_PERSISTENCE) final BlobPersistence blobPersistence,
            final JsonUtil jsonUtil) {
        return new BlobRecursiveChainOpeningGrantStore(blobPersistence, jsonUtil);
    }

    @Bean
    public RecursiveJobStateStore recursiveJobStateStore(
            @Qualifier(JOB_BLOB_PERSISTENCE) final BlobPersistence blobPersistence,
            final JsonUtil jsonUtil) {
        return new BlobRecursiveJobStateStore(blobPersistence, jsonUtil);
    }

    @Bean
    public RecursiveChainOpeningGrantService recursiveChainOpeningGrantService(
            final RecursiveChainOpeningGrantStore grantStore) {
        return new RecursiveChainOpeningGrantService(grantStore);
    }

    @Bean
    public RecursiveTraversalService recursiveTraversalService(
            final DigitalTwinRegistryService digitalTwinRegistryService,
            final EdcSubmodelFacade submodelFacade,
            final JsonUtil jsonUtil) {
        return new RecursiveTraversalService(digitalTwinRegistryService, submodelFacade, jsonUtil);
    }

    @Bean
    public RecursiveSubmodelCollector recursiveSubmodelCollector(
            final DigitalTwinRegistryService digitalTwinRegistryService,
            final EdcSubmodelFacade submodelFacade) {
        return new RecursiveSubmodelCollector(
                digitalTwinRegistryService, submodelFacade, new ObjectMapper().findAndRegisterModules());
    }

    @Bean
    public RecursiveNotificationSender recursiveNotificationSender(
            final ConnectorEndpointsService connectorEndpointsService,
            final EdcConfiguration edcConfiguration,
            final EdcOrchestrator edcOrchestrator,
            final EdcDataPlaneClient edcDataPlaneClient,
            final PolicyCheckerService policyCheckerService) {
        return new EdcRecursiveNotificationSender(
                connectorEndpointsService, edcConfiguration, edcOrchestrator, edcDataPlaneClient,
                policyCheckerService);
    }

    @Bean
    public RecursiveNotificationMessageValidator recursiveNotificationMessageValidator(
            final Validator validator, final ObjectMapper objectMapper) {
        return new RecursiveNotificationMessageValidator(validator, objectMapper);
    }

    @Bean(name = "recursiveJobExecutor", destroyMethod = "shutdown")
    public ExecutorService recursiveJobExecutor(
            @Value("${irs.job.recursive.threadCount:${irs.job.cached.threadCount:5}}") final int threadCount) {
        return Executors.newFixedThreadPool(threadCount);
    }

    @Bean
    public RecursiveJobService recursiveJobService(
            final RecursiveChainOpeningGrantService grantService,
            final RecursiveTraversalService traversalService,
            final RecursiveJobStateStore jobStateStore,
            final RecursiveNotificationSender notificationSender,
            final RecursiveSubmodelCollector submodelCollector,
            final RecursiveProperties properties,
            @Qualifier("recursiveJobExecutor") final Executor recursiveJobExecutor,
            final Clock clock) {
        return new RecursiveJobService(grantService, traversalService, jobStateStore,
                notificationSender, submodelCollector, properties, recursiveJobExecutor, clock);
    }

    @Bean
    public RecursiveNotificationReceiver recursiveNotificationReceiver(final RecursiveJobService recursiveJobService,
            final RecursiveNotificationMessageValidator messageValidator) {
        return new RecursiveNotificationReceiver(recursiveJobService, messageValidator);
    }

    @Bean
    public RecursiveTimeoutMonitor recursiveTimeoutMonitor(final RecursiveJobService recursiveJobService,
            final RecursiveProperties properties) {
        return new RecursiveTimeoutMonitor(recursiveJobService, properties);
    }

    @Bean
    public RecursiveStartupRecovery recursiveStartupRecovery(final RecursiveJobService recursiveJobService,
            @Qualifier("recursiveJobExecutor") final Executor recursiveJobExecutor) {
        return new RecursiveStartupRecovery(recursiveJobService, recursiveJobExecutor);
    }

}
