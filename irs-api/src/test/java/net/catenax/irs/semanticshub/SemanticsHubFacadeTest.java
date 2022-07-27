package net.catenax.irs.semanticshub;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

class SemanticsHubFacadeTest {

    private SemanticsHubFacade semanticsHubFacade = new SemanticsHubFacade(new SemanticsHubClientLocalStub());

    @Test
    void shouldReturnModelJsonSchema() {
        final String modelUrn = SchemaModel.SerialPartTypization.getUrn();

        final Map<String, Object> modelJsonSchema = semanticsHubFacade.getModelJsonSchema(modelUrn);

        assertThat(modelJsonSchema).isNotEmpty();
    }

}
