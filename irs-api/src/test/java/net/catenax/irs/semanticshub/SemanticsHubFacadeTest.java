package net.catenax.irs.semanticshub;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SemanticsHubFacadeTest {

    private final SemanticsHubFacade semanticsHubFacade = new SemanticsHubFacade(new SemanticsHubClientLocalStub());

    @Test
    void shouldReturnModelJsonSchema() {
        final String defaultUrn = "urn:bamm:io.catenax.serial_part_typization:1.0.0#SerialPartTypization";

        final String modelJsonSchema = semanticsHubFacade.getModelJsonSchema(defaultUrn);

        assertThat(modelJsonSchema).isNotBlank();
    }

}
