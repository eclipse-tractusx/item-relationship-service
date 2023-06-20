package org.eclipse.tractusx.irs.aaswrapper.job;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ExtractIdFromSubprotocolBodyTest {

    @Test
    void shouldExtractIdFromSubprotocol() {
        // given
        final String exampleSubprotocol = "id=9300395e-c0a5-4e88-bc57-a3973fec4c26;idsEndpoint=http://edc.control.plane/";

        // when
        final String actual = ExtractIdFromSubprotocolBody.extractAssetId(exampleSubprotocol);

        // then
        assertThat(actual).isEqualTo("9300395e-c0a5-4e88-bc57-a3973fec4c26");
    }

}