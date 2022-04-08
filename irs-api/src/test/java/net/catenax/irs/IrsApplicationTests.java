package net.catenax.irs;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.nio.file.Files;

import org.hibernate.validator.internal.IgnoreForbiddenApisErrors;
import org.junit.Ignore;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class IrsApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void contextLoads() {
    }


    @Disabled
    @Test
    void generatedOpenApiMatchesContract() throws Exception {
        assertThat(this.restTemplate.getForObject("http://localhost:" + port + "/api/api-docs.yaml",
                String.class))
                .isEqualToNormalizingNewlines(Files.readString(new File("../api/irs-v0.2.yaml").toPath(), UTF_8));
    }
}
