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

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.tractusx.irs.configuration.JobConfiguration.JOB_BLOB_PERSISTENCE;
import static org.eclipse.tractusx.irs.recursive.RecursiveTestConstants.ATLAS_BPNL;
import static org.eclipse.tractusx.irs.recursive.config.RecursiveChainOpeningGrantConfiguration.CHAIN_OPENING_GRANT_BLOB_PERSISTENCE;
import static org.mockito.Mockito.mock;

import java.time.Clock;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.tractusx.irs.InMemoryBlobStore;
import org.eclipse.tractusx.irs.common.persistence.BlobPersistence;
import org.eclipse.tractusx.irs.edc.client.EdcConfiguration;
import org.eclipse.tractusx.irs.edc.client.EdcDataPlaneClient;
import org.eclipse.tractusx.irs.edc.client.EdcOrchestrator;
import org.eclipse.tractusx.irs.edc.client.EdcSubmodelFacade;
import org.eclipse.tractusx.irs.edc.client.policy.PolicyCheckerService;
import org.eclipse.tractusx.irs.recursive.controller.RecursiveChainOpeningGrantController;
import org.eclipse.tractusx.irs.recursive.controller.RecursiveExceptionHandler;
import org.eclipse.tractusx.irs.recursive.controller.RecursiveHttpExceptionResolver;
import org.eclipse.tractusx.irs.recursive.controller.RecursiveJobController;
import org.eclipse.tractusx.irs.recursive.controller.RecursiveNotificationController;
import org.eclipse.tractusx.irs.registryclient.DigitalTwinRegistryService;
import org.eclipse.tractusx.irs.registryclient.discovery.ConnectorEndpointsService;
import org.eclipse.tractusx.irs.util.JsonUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class RecursiveConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class,
                    ValidationAutoConfiguration.class))
            .withUserConfiguration(RecursiveConfiguration.class, RecursiveChainOpeningGrantConfiguration.class,
                    RecursiveJobController.class, RecursiveNotificationController.class,
                    RecursiveChainOpeningGrantController.class, RecursiveExceptionHandler.class,
                    RecursiveHttpExceptionResolver.class)
            .withBean(ObjectMapper.class, ObjectMapper::new);

    @Test
    void shouldDisableRecursiveModuleByDefault() {
        contextRunner.run(RecursiveConfigurationTest::assertRecursiveModuleDisabled);
    }

    @Test
    void shouldEnableRecursiveModuleWhenConfigured() {
        enabledContext().run(context -> {
            assertThat(context).hasNotFailed().hasSingleBean(RecursiveProperties.class).hasBean("recursiveJobService")
                    .hasBean("recursiveChainOpeningGrantStore").hasBean("recursiveJobStateStore")
                    .hasBean("recursiveJobExecutor").hasBean("recursiveTimeoutMonitor")
                    .hasBean("recursiveStartupRecovery")
                    .hasSingleBean(RecursiveJobController.class).hasSingleBean(RecursiveNotificationController.class)
                    .hasSingleBean(RecursiveChainOpeningGrantController.class)
                    .hasSingleBean(RecursiveExceptionHandler.class)
                    .hasSingleBean(RecursiveHttpExceptionResolver.class);
        });
    }

    @Test
    void shouldDisableRecursiveModuleWithoutItsDependenciesOrConfiguration() {
        contextRunner.withPropertyValues("irs.recursive.enabled=false", "irs.recursive.localBpnl=",
                        "irs.recursive.timeout.defaultJobTtl=invalid", "blobstore.persistence.storeType=MINIO")
                .run(RecursiveConfigurationTest::assertRecursiveModuleDisabled);
    }

    @Test
    void shouldRequireLocalBpnlWhenEnabled() {
        enabledContext().withPropertyValues("irs.recursive.localBpnl=").run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure()).hasRootCauseInstanceOf(BindValidationException.class);
        });
    }

    private static void assertRecursiveModuleDisabled(final AssertableApplicationContext context) {
        assertThat(context).hasNotFailed().hasSingleBean(RecursiveHttpExceptionResolver.class)
                .doesNotHaveBean(RecursiveProperties.class)
                .doesNotHaveBean(RecursiveConfiguration.class)
                .doesNotHaveBean(RecursiveChainOpeningGrantConfiguration.class)
                .doesNotHaveBean(RecursiveJobController.class)
                .doesNotHaveBean(RecursiveNotificationController.class)
                .doesNotHaveBean(RecursiveChainOpeningGrantController.class)
                .doesNotHaveBean(RecursiveExceptionHandler.class)
                .doesNotHaveBean("recursiveJobService")
                .doesNotHaveBean("recursiveJobStateStore")
                .doesNotHaveBean("recursiveChainOpeningGrantStore")
                .doesNotHaveBean("recursiveJobExecutor")
                .doesNotHaveBean("recursiveTimeoutMonitor")
                .doesNotHaveBean("recursiveStartupRecovery")
                .doesNotHaveBean(CHAIN_OPENING_GRANT_BLOB_PERSISTENCE);
    }

    private ApplicationContextRunner enabledContext() {
        return contextRunner.withPropertyValues("irs.recursive.enabled=true", "irs.recursive.localBpnl=" + ATLAS_BPNL)
                .withBean(JOB_BLOB_PERSISTENCE, BlobPersistence.class, InMemoryBlobStore::new)
                .withBean(CHAIN_OPENING_GRANT_BLOB_PERSISTENCE, BlobPersistence.class, InMemoryBlobStore::new)
                .withBean(JsonUtil.class, JsonUtil::new)
                .withBean(Clock.class, Clock::systemUTC)
                .withBean(DigitalTwinRegistryService.class, () -> mock(DigitalTwinRegistryService.class))
                .withBean(EdcSubmodelFacade.class, () -> mock(EdcSubmodelFacade.class))
                .withBean(ConnectorEndpointsService.class, () -> mock(ConnectorEndpointsService.class))
                .withBean(EdcConfiguration.class, EdcConfiguration::new)
                .withBean(EdcOrchestrator.class, () -> mock(EdcOrchestrator.class))
                .withBean(EdcDataPlaneClient.class, () -> mock(EdcDataPlaneClient.class))
                .withBean(PolicyCheckerService.class, () -> mock(PolicyCheckerService.class));
    }
}
