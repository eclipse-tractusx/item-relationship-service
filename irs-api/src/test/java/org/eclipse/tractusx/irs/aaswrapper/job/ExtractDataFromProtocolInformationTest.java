package org.eclipse.tractusx.irs.aaswrapper.job;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;

class ExtractDataFromProtocolInformationTest {

    @Test
    void shouldExtractIdFromSubprotocol() {
        // given
        final String exampleSubprotocol = "other_id=fake-id;id=12345;idsEndpoint=http://edc.control.plane/";

        // when
        final String actual = ExtractDataFromProtocolInformation.extractAssetId(exampleSubprotocol);

        // then
        assertThat(actual).isEqualTo("12345");
    }

    @Test
    void shouldExtractSufixPathFromHref() throws URISyntaxException {
        // given
        final String href = "https://edc.data.plane/shells/123/submodels/456/submodel";

        // when
        final String actual = ExtractDataFromProtocolInformation.extractSuffix(href);

        // then
        assertThat(actual).isEqualTo("/shells/123/submodels/456/submodel");
    }

}