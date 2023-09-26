package org.eclipse.tractusx.irs.aaswrapper.job;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ExtractDataFromProtocolInformationTest {

    @Test
    void shouldExtractIdFromSubprotocol() {
        // given
        final String exampleSubprotocol = "other_id=fake-id;id=12345;dspEndpoint=http://edc.control.plane/";

        // when
        final String actual = ExtractDataFromProtocolInformation.extractAssetId(exampleSubprotocol);

        // then
        assertThat(actual).isEqualTo("12345");
    }

    @Test
    void shouldExtractDspEndpointFromSubprotocol() {
        // given
        final String exampleSubprotocol = "other_id=fake-id;id=12345;dspEndpoint=http://edc.control.plane/";

        // when
        final Optional<String> actual = ExtractDataFromProtocolInformation.extractDspEndpoint(exampleSubprotocol);

        // then
        assertThat(actual).isPresent().contains("http://edc.control.plane/");
    }

    @ParameterizedTest()
    @ValueSource(strings = { "other_id=fake-id;id=12345;dspEndpoint=",
                             "other_id=fake-id;id=12345;dspEndpoint",
                             "other_id=fake-id;id=12345;",
                             "other_id=fake-id;id=12345"
    })
    void shouldReturnEmptyIfDspEndpointMissing(final String subprotocolBody) {

        // when
        final Optional<String> actual = ExtractDataFromProtocolInformation.extractDspEndpoint(subprotocolBody);

        // then
        assertThat(actual).isEmpty();
    }

}