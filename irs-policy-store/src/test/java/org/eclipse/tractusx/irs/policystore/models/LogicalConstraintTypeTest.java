package org.eclipse.tractusx.irs.policystore.models;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

class LogicalConstraintTypeTest {

    @Test
    void shouldCreateLogicalConstraintTypeFromString() {
        assertThat(LogicalConstraintType.fromValue("and")).isEqualTo(LogicalConstraintType.AND);
    }

    @Test
    void shouldThrowExceptionForInvalidString() {
        assertThatThrownBy(() -> LogicalConstraintType.fromValue("abcd"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Unsupported LogicalConstraintType: abcd");
    }

    @Test
    void shouldPrintProperString() {
        assertThat(LogicalConstraintType.OR.toString()).isEqualTo(LogicalConstraintType.OR.getCode());
    }

}