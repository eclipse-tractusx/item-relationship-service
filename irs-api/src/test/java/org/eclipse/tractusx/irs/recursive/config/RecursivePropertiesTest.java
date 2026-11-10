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
import static org.eclipse.tractusx.irs.recursive.RecursiveTestConstants.ATLAS_BPNL;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

class RecursivePropertiesTest {

    private static final String LOCAL_BPNL_PROPERTY = "irs.recursive.localBpnl=" + ATLAS_BPNL;

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
            .withUserConfiguration(TestConfiguration.class);

    @ParameterizedTest
    @ValueSource(strings = { ATLAS_BPNL, "BPNL0000atls0001" })
    void shouldBindConfiguredLocalBpnlWithoutChangingCase(final String bpnl) {
        contextRunner
                .withPropertyValues("irs.recursive.localBpnl=" + bpnl)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context.getBean(RecursiveProperties.class).getLocalBpnl())
                            .isEqualTo(bpnl);
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

    @ParameterizedTest
    @CsvSource(value = {
            "defaultJobTtl|PT0S|defaultJobTtl must be positive",
            "defaultJobTtl|-PT0.000000001S|defaultJobTtl must be positive",
            "maxJobTtl|PT0S|maxJobTtl must be positive",
            "maxJobTtl|-PT0.000000001S|maxJobTtl must be positive",
            "childResponseSafetyBuffer|-PT0.000000001S|childResponseSafetyBuffer must not be negative",
            "timeoutCheckInterval|PT0S|timeoutCheckInterval must be at least 1 ms",
            "timeoutCheckInterval|PT0.000999999S|timeoutCheckInterval must be at least 1 ms",
            "timeoutCheckInterval|-PT1S|timeoutCheckInterval must be at least 1 ms",
            "timeoutCheckInterval|PT9223372036854775.808S|timeoutCheckInterval must be at least 1 ms"
    }, delimiter = '|')
    void shouldRejectInvalidTimeoutAtStartup(final String property, final String value, final String diagnostic) {
        contextRunner.withPropertyValues(LOCAL_BPNL_PROPERTY, "irs.recursive.timeout." + property + "=" + value)
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).hasRootCauseInstanceOf(BindValidationException.class);
                    assertThat(rootCause(context.getStartupFailure())).hasMessageContaining(diagnostic);
                });
    }

    @Test
    void shouldRejectDefaultTtlAboveMaximumAtStartup() {
        contextRunner.withPropertyValues(LOCAL_BPNL_PROPERTY, "irs.recursive.timeout.defaultJobTtl=PT2H0.000000001S")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).hasRootCauseInstanceOf(BindValidationException.class);
                    assertThat(rootCause(context.getStartupFailure()))
                            .hasMessageContaining("defaultJobTtl must not exceed maxJobTtl");
                });
    }

    @ParameterizedTest
    @CsvSource(value = {
            "childResponseSafetyBuffer|PT0S",
            "defaultJobTtl|PT0.000000001S",
            "timeoutCheckInterval|PT0.001S",
            "timeoutCheckInterval|PT9223372036854775.807999999S"
    }, delimiter = '|')
    void shouldAcceptTimeoutBoundariesAtStartup(final String property, final String value) {
        contextRunner.withPropertyValues(LOCAL_BPNL_PROPERTY, "irs.recursive.timeout." + property + "=" + value)
                .run(context -> assertThat(context).hasNotFailed());
    }

    @Test
    void shouldAcceptShortTtlWithLargerSafetyBufferAtStartup() {
        contextRunner.withPropertyValues(LOCAL_BPNL_PROPERTY, "irs.recursive.timeout.defaultJobTtl=PT0.000000001S",
                        "irs.recursive.timeout.maxJobTtl=PT0.000000001S",
                        "irs.recursive.timeout.childResponseSafetyBuffer=PT1H")
                .run(context -> assertThat(context).hasNotFailed());
    }

    @Test
    void shouldRejectNullTimeoutAndDurations() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            final Validator validator = factory.getValidator();
            final RecursiveProperties properties = new RecursiveProperties();
            properties.setLocalBpnl(ATLAS_BPNL);
            properties.setTimeout(null);
            assertThat(validator.validate(properties)).extracting(violation -> violation.getPropertyPath().toString())
                    .containsExactly("timeout");

            final RecursiveProperties.Timeout timeout = new RecursiveProperties.Timeout();
            timeout.setDefaultJobTtl(null);
            timeout.setMaxJobTtl(null);
            timeout.setChildResponseSafetyBuffer(null);
            timeout.setTimeoutCheckInterval(null);
            properties.setTimeout(timeout);
            assertThat(validator.validate(properties)).extracting(violation -> violation.getPropertyPath().toString())
                    .containsExactlyInAnyOrder("timeout.defaultJobTtl", "timeout.maxJobTtl",
                            "timeout.childResponseSafetyBuffer", "timeout.timeoutCheckInterval");
        }
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
