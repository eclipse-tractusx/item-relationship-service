package org.eclipse.tractusx.irs.aaswrapper.job;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;

class ExtractIdFromProtocolInformationTest {

    @Test
    void shouldExtractIdFromSubprotocol() {
        // given
        final String exampleSubprotocol = "id=9300395e-c0a5-4e88-bc57-a3973fec4c26;idsEndpoint=http://edc.control.plane/";

        // when
        final String actual = ExtractIdFromProtocolInformation.extractAssetId(exampleSubprotocol);

        // then
        assertThat(actual).isEqualTo("9300395e-c0a5-4e88-bc57-a3973fec4c26");
    }

    @Test
    void shouldExtractSufixPathFromHref() throws URISyntaxException {
        // given
        final String href = "https://edc.data.plane/shells/123/submodels/456/submodel";

        // when
        final String actual = ExtractIdFromProtocolInformation.extractSuffix(href);

        // then
        assertThat(actual).isEqualTo("/shells/123/submodels/456/submodel");
    }

}