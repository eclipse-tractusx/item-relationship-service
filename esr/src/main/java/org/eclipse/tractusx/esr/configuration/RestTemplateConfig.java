package org.eclipse.tractusx.esr.configuration;

import java.time.Duration;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@RequiredArgsConstructor
public class RestTemplateConfig {

    private static final int TIMEOUT_SECONDS = 90;

    @Bean
        RestTemplate tokenFromContextRestTemplate(final RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.setReadTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                                  .setConnectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                                  .build();
    }

}
