package net.catenax.irs.component.tombstone;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import io.github.resilience4j.retry.RetryRegistry;
import net.catenax.irs.component.ProcessingError;
import net.catenax.irs.component.Tombstone;
import org.junit.jupiter.api.Test;

class TombStoneTest {

    Tombstone tombstone;

    @Test
    void fromTombstoneTest() {
        // arrange
        String catenaXId = "5e3e9060-ba73-4d5d-a6c8-dfd5123f4d99";
        IllegalArgumentException illegalArgumentException = new IllegalArgumentException("Some funny error occur");
        String endPointUrl = "http://localhost/dummy/interfaceinformation/urn:uuid:8a61c8db-561e-4db0-84ec-a693fc5ffdf6";

        ProcessingError processingError = ProcessingError.builder()
                                                         .withRetryCounter(RetryRegistry.ofDefaults()
                                                                                        .getDefaultConfig()
                                                                                        .getMaxAttempts())
                                                         .withLastAttempt(ZonedDateTime.now(ZoneOffset.UTC))
                                                         .withErrorDetail("Some funny error occur")
                                                         .build();

        Tombstone expectedTombstone = Tombstone.builder()
                                               .catenaXId(catenaXId)
                                               .endpointURL(endPointUrl)
                                               .processingError(processingError)
                                               .build();

        //act
        tombstone = Tombstone.from(catenaXId, endPointUrl, illegalArgumentException,
                RetryRegistry.ofDefaults().getDefaultConfig().getMaxAttempts());

        // assert
        assertThat(tombstone).isNotNull();
        assertThat(tombstone.getProcessingError().getErrorDetail()).isEqualTo(processingError.getErrorDetail());
        assertThat(tombstone.getProcessingError().getRetryCounter()).isEqualTo(processingError.getRetryCounter());
        assertThat(zonedDateTimeExcerpt(tombstone.getProcessingError().getLastAttempt())).isEqualTo(
                zonedDateTimeExcerpt(processingError.getLastAttempt()));
        assertThat(tombstone.getCatenaXId()).isEqualTo(expectedTombstone.getCatenaXId());
        assertThat(tombstone.getEndpointURL()).isEqualTo(expectedTombstone.getEndpointURL());
        assertThat(tombstone.getProcessingError().getRetryCounter()).isEqualTo(
                expectedTombstone.getProcessingError().getRetryCounter());
    }

    private String zonedDateTimeExcerpt(ZonedDateTime dateTime) {
        return new StringBuilder().append(dateTime.getYear())
                                  .append("-")
                                  .append(dateTime.getMonth())
                                  .append("-")
                                  .append(dateTime.getDayOfMonth())
                                  .append("T")
                                  .append(dateTime.getHour())
                                  .append(":")
                                  .append(dateTime.getMinute())
                                  .append(":")
                                  .append(dateTime.getSecond())
                                  .toString();
    }

}
