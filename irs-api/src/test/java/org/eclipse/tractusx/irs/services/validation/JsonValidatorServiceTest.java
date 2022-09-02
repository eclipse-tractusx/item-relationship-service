package org.eclipse.tractusx.irs.services.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import org.eclipse.tractusx.irs.util.JsonUtil;
import org.junit.jupiter.api.Test;

class JsonValidatorServiceTest {

    private final JsonValidatorService testee = new JsonValidatorService(new JsonUtil());

    @Test
    void shouldValidateAssemblyPartRelationship() throws Exception {
        final String schema = readFile("/json-schema/assemblyPartRelationship-v1.1.0.json");
        final String payload = readFile("/__files/assemblyPartRelationship.json");

        final ValidationResult result = testee.validate(schema, payload);

        assertThat(result.isValid()).isTrue();
    }

    @Test
    void shouldNotValidateEmptyString() throws Exception {
        final String schema = readFile("/json-schema/assemblyPartRelationship-v1.1.0.json");
        final String payload = "";

        final ValidationResult result = testee.validate(schema, payload);

        assertThat(result.isValid()).isFalse();
    }

    @Test
    void shouldNotValidateEmptyJson() throws Exception {
        final String schema = readFile("/json-schema/assemblyPartRelationship-v1.1.0.json");
        final String payload = "{}";

        final ValidationResult result = testee.validate(schema, payload);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getValidationErrors()).isNotEmpty();
    }

    @Test
    void shouldThrowExceptionOnIllegalSchema() throws Exception {
        final String schema = readFile("/json-schema/invalid.json");
        final String payload = "{}";

        assertThatThrownBy(() -> testee.validate(schema, payload)).isInstanceOf(InvalidSchemaException.class);
    }

    private String readFile(final String path) throws IOException, URISyntaxException {
        final URL resource = getClass().getResource(path);
        Objects.requireNonNull(resource);
        return Files.readString(Path.of(resource.toURI()));
    }
}