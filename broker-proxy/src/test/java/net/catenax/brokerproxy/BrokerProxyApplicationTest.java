package net.catenax.brokerproxy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;

import java.io.File;
import java.nio.file.Files;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BrokerProxyApplicationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void contextLoads() {
    }

    @Test
    void generatedOpenApiMatchesContract() throws Exception {
        assertThat(this.restTemplate.getForObject("http://localhost:" + port + "/broker-proxy/api-docs.yaml",
                String.class))
                .isEqualToNormalizingNewlines(Files.readString(new File("../api/brokerproxy-v0.1.yaml").toPath(), UTF_8));
    }
}
