package org.eclipse.tractusx.irs.policystore.models;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.NoSuchElementException;

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

}