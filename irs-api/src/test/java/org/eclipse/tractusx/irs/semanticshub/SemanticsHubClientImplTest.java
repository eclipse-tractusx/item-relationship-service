package org.eclipse.tractusx.irs.semanticshub;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

class SemanticsHubClientImplTest {

    private final RestTemplate restTemplate = mock(RestTemplate.class);
    private final SemanticsHubClientImpl semanticsHubClient = new SemanticsHubClientImpl(restTemplate, "url");

    @Test
    void shouldCallExternalServiceOnceAndGetJsonSchema() {
        final String jsonSchemaMock = "{\"$schema\": \"http://json-schema.org/draft-07/schema#\", \"type\": \"integer\"}";
        doReturn(jsonSchemaMock).when(restTemplate).getForObject(any(), eq(String.class));

        final String resultJsonSchema = semanticsHubClient.getModelJsonSchema("urn");

        assertThat(resultJsonSchema).isNotBlank();
        assertThat(resultJsonSchema).contains("http://json-schema.org/draft-07/schema#");
        verify(this.restTemplate, times(1)).getForObject(any(), eq(String.class));
    }

}
