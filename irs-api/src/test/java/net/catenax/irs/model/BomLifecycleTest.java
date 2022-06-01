package net.catenax.irs.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.NoSuchElementException;

import net.catenax.irs.component.enums.BomLifecycle;
import org.junit.jupiter.api.Test;

class BomLifecycleTest {

    @Test
    void shouldReturnBomLifecycleObjectFromConstantString() {
        final String constant = BomLifecycle.AS_BUILT.getValue();

        final BomLifecycle bomLifecycle = BomLifecycle.fromValue(constant);

        assertThat(bomLifecycle).isEqualTo(BomLifecycle.AS_BUILT);
    }

    @Test
    void shouldThrowNoSuchElementException() {
        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> BomLifecycle.fromValue("Malformed"));
    }
}
