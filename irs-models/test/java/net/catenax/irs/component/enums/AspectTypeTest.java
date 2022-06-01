package net.catenax.irs.component.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

class AspectTypeTest {

    @Test
    void shouldReturnAspectTypeObjectFromConstantString() {
        final String constant = AspectType.AspectTypesConstants.ASSEMBLY_PART_RELATIONSHIP;

        final AspectType aspectType = AspectType.fromValue(constant);

        assertThat(aspectType).isEqualTo(AspectType.ASSEMBLY_PART_RELATIONSHIP);
    }

    @Test
    void shouldThrowNoSuchElementException() {
        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> AspectType.fromValue("Malformed"));
    }
}
