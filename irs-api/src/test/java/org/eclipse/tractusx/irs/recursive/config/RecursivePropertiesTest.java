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

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

class RecursivePropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
            .withUserConfiguration(TestConfiguration.class);

    @Test
    void shouldBindConfiguredLocalBpnl() {
        contextRunner
                .withPropertyValues("irs.recursive.localBpnl=BPNL0000atls0001")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context.getBean(RecursiveProperties.class).getLocalBpnl())
                            .isEqualTo("BPNL0000atls0001");
                });
    }

    @Test
    void shouldRejectBlankLocalBpnlAtStartup() {
        contextRunner
                .withPropertyValues("irs.recursive.localBpnl=")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).hasRootCauseInstanceOf(BindValidationException.class);
                    assertThat(rootCause(context.getStartupFailure()))
                            .hasMessageContaining("localBpnl")
                            .hasMessageContaining("must be configured");
                });
    }

    @Test
    void shouldRejectInvalidLocalBpnlAtStartup() {
        contextRunner
                .withPropertyValues("irs.recursive.localBpnl=BPNS000000000001")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).hasRootCauseInstanceOf(BindValidationException.class);
                    assertThat(rootCause(context.getStartupFailure()))
                            .hasMessageContaining("localBpnl")
                            .hasMessageContaining("must be a BPNL");
                });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(RecursiveProperties.class)
    static class TestConfiguration {
    }

    private static Throwable rootCause(final Throwable throwable) {
        Throwable result = throwable;
        while (result.getCause() != null) {
            result = result.getCause();
        }
        return result;
    }

}
