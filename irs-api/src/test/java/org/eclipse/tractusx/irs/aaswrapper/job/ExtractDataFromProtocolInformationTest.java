package org.eclipse.tractusx.irs.aaswrapper.job;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

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
        final String actual = ExtractDataFromProtocolInformation.extractDspEndpoint(exampleSubprotocol);

        // then
        assertThat(actual).isEqualTo("http://edc.control.plane/");
    }

}