package net.catenax.irs.semanticshub;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SemanticsHubFacadeTest {

    private final SemanticsHubFacade semanticsHubFacade = new SemanticsHubFacade(new SemanticsHubClientLocalStub());

    @Test
    void shouldReturnModelJsonSchema() {
        final String modelUrn = SchemaModel.SERIAL_PART_TYPIZATION.getUrn();

        final String modelJsonSchema = semanticsHubFacade.getModelJsonSchema(modelUrn);

        assertThat(modelJsonSchema).isNotBlank();
    }

}
