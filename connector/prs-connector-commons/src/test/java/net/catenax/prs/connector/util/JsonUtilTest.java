package net.catenax.prs.connector.util;

import org.eclipse.dataspaceconnector.monitor.ConsoleMonitor;
import org.eclipse.dataspaceconnector.spi.EdcException;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JsonUtilTest {

    private JsonUtil sut = new JsonUtil(new ConsoleMonitor());

    @Test
    void asString_onSuccess() {
        assertThat(sut.asString(new HashMap<String, String>()))
                .isEqualTo("{}");
    }

    @Test
    void asString_onFailure() {
        Object mockItem = mock(Object.class);
        when(mockItem.toString()).thenReturn(mockItem.getClass().getName());
        assertThatExceptionOfType(EdcException.class)
                .isThrownBy(() -> sut.asString(mockItem));
    }

    @Test
    void fromString_OnSuccess() {
        assertThat(sut.fromString("{}", HashMap.class))
                .isEqualTo(new HashMap<String, String>());
    }

    @Test
    void fromString_OnFailure() {
        assertThatExceptionOfType(EdcException.class)
                .isThrownBy(() -> sut.fromString("{", HashMap.class));
    }
}