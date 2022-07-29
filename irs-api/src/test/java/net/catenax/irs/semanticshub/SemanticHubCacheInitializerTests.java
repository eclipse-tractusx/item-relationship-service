package net.catenax.irs.semanticshub;

import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import net.catenax.irs.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(TestConfig.class)
@ActiveProfiles(profiles = { "test" })
class SemanticHubCacheInitializerTests {

    @Autowired
    CacheManager cacheManager;

    @Autowired
    SemanticsHubCacheInitializer semanticsHubCacheInitializer;

    private Optional<String> getJsonSchemaFromCache() {
        final String defaultUrn = "urn:bamm:io.catenax.serial_part_typization:1.0.0#SerialPartTypization";

        return ofNullable(cacheManager.getCache("schema_cache")).map(
                cache -> cache.get(defaultUrn, String.class));
    }

    @Test
    void shouldFindJsonSchemaInCacheAfterSpringContextStartup() {
        final Optional<String> jsonSchemaFromCache = getJsonSchemaFromCache();

        assertThat(jsonSchemaFromCache).isNotEmpty();
        assertThat(jsonSchemaFromCache.get()).contains("http://json-schema.org/draft-07/schema#");
    }

    @Test
    void shouldFindJsonSchemaInCacheAfterReinitialization() {
        semanticsHubCacheInitializer.reinitializeAllCacheInterval();

        final Optional<String> jsonSchemaFromCache = getJsonSchemaFromCache();

        assertThat(jsonSchemaFromCache).isNotEmpty();
        assertThat(jsonSchemaFromCache.get()).contains("http://json-schema.org/draft-07/schema#");
    }

}