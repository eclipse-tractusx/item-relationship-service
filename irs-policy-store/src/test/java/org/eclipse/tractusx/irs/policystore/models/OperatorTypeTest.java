package org.eclipse.tractusx.irs.policystore.models;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.NoSuchElementException;

import org.eclipse.tractusx.irs.edc.client.policy.OperatorType;
import org.junit.jupiter.api.Test;

class OperatorTypeTest {

    @Test
    void shouldCreateOperatorTypeFromString() {
        assertThat(OperatorType.fromValue("gteq")).isEqualTo(OperatorType.GTEQ);
    }

    @Test
    void shouldThrowExceptionForInvalidString() {
        assertThatThrownBy(() -> OperatorType.fromValue("abcd"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Unsupported OperatorType: abcd");
    }

    @Test
    void shouldPrintProperString() {
        assertThat(OperatorType.GTEQ.toString()).isEqualTo(OperatorType.GTEQ.getCode());
    }

    @Test
    void whenFromValueShouldRemovePrefixIfPresent() {
        // given
        final String prefix = "odrl:";
        final String operator = "eq";

        // when
        final OperatorType result = OperatorType.fromValue(prefix + operator);

        // then
        assertThat(result.toString().toLowerCase()).isEqualTo(operator);
    }

    @Test
    void whenFromValueShouldNotRemovePrefixIfNotPresent() {
        // given
        final String operator = "eq";

        // when
        final OperatorType result = OperatorType.fromValue(operator);

        // then
        assertThat(result.toString().toLowerCase()).isEqualTo(operator);
    }
}