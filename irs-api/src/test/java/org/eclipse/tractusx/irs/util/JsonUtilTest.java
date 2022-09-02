package org.eclipse.tractusx.irs.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;

import org.eclipse.tractusx.irs.exceptions.JsonParseException;
import org.junit.jupiter.api.Test;

class JsonUtilTest {

    private final JsonUtil sut = new JsonUtil();

    @Test
    void asString_onSuccess() {
        assertThat(sut.asString(new HashMap<String, String>())).isEqualTo("{}");
    }

    @Test
    void asString_onFailure() {
        Object mockItem = mock(Object.class);
        when(mockItem.toString()).thenReturn(mockItem.getClass().getName());
        assertThatExceptionOfType(JsonParseException.class).isThrownBy(() -> sut.asString(mockItem));
    }

    @Test
    void fromString_OnSuccess() {
        assertThat(sut.fromString("{}", HashMap.class)).isEqualTo(new HashMap<String, String>());
    }

    @Test
    void fromString_OnFailure() {
        assertThatExceptionOfType(JsonParseException.class).isThrownBy(() -> sut.fromString("{", HashMap.class));
    }
}