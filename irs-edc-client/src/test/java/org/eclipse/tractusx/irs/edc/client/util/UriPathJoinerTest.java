package org.eclipse.tractusx.irs.edc.client.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class UriPathJoinerTest {

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = { "",
                             "not a valid url"
    })
    void shouldThrowExceptionWhenBaseUrlIsInvalid(String invalidUrl) {
        // Arrange
        final String pathToAppend = "/path/to/append";

        // Act & Assert
        assertThrows(URISyntaxException.class, () -> UriPathJoiner.appendPath(invalidUrl, pathToAppend));
    }

    @Test
    void shouldReturnBaseUrlWhenAppendedPathIsNull() throws URISyntaxException {
        // Arrange
        final String url = "http://localhost/";
        final String pathToAppend = null;

        // Act
        final String result = UriPathJoiner.appendPath(url, pathToAppend);

        // Assert
        assertThat(result).isEqualTo("http://localhost/");
    }

    @Test
    void shouldReturnBaseUrlWhenAppendedPathIsEmpty() throws URISyntaxException {
        // Arrange
        final String url = "https://example.com/path";
        final String pathToAppend = "";

        // Act
        final String result = UriPathJoiner.appendPath(url, pathToAppend);

        // Assert
        assertThat(result).isEqualTo("https://example.com/path");
    }

    @Test
    void shouldCorrectlyAppendPathToAUrlWithoutAPath() throws URISyntaxException {
        // Arrange
        final String url = "https://example.com";
        final String pathToAppend = "path/to/append";

        // Act
        final String result = UriPathJoiner.appendPath(url, pathToAppend);

        // Assert
        assertThat(result).isEqualTo("https://example.com/path/to/append");
    }

    @Test
    void shouldCorrectlyAppendAndTrimPath() throws URISyntaxException {
        // Arrange
        final String url = "https://example.com/";
        final String pathToAppend = "path/to/append";

        // Act
        final String result = UriPathJoiner.appendPath(url, pathToAppend);

        // Assert
        assertThat(result).isEqualTo("https://example.com/path/to/append");
    }

    @Test
    void shouldCorrectlyAppendPathToAUrlWithATrailingSlash() throws URISyntaxException {
        // Arrange
        final String url = "https://example.com/";
        final String pathToAppend = "path/to/append";

        // Act
        final String result = UriPathJoiner.appendPath(url, pathToAppend);

        // Assert
        assertThat(result).isEqualTo("https://example.com/path/to/append");
    }

    @Test
    void shouldCorrectlyAppendPathToAUrlWithQueryString() throws URISyntaxException {
        // Arrange
        final String url = "https://example.com/path?queryParam=value";
        final String pathToAppend = "newPath";

        // Act
        final String result = UriPathJoiner.appendPath(url, pathToAppend);

        // Assert
        assertThat(result).isEqualTo("https://example.com/path/newPath?queryParam=value");
    }

    @Test
    void shouldCorrectlyAppendPathToAUrlWithFragmentIdentifier() throws URISyntaxException {
        // Arrange
        final String url = "https://example.com/path#fragment";
        final String pathToAppend = "appendix";

        // Act
        final String result = UriPathJoiner.appendPath(url, pathToAppend);

        // Assert
        assertThat(result).isEqualTo("https://example.com/path/appendix#fragment");
    }

}