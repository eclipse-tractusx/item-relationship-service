package org.eclipse.tractusx.irs.aaswrapper.job;

import static org.assertj.core.api.Assertions.assertThat;

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
    void shouldExtractSufixPathFromHref() {
        // given
        final String href = "https://edc.data.plane/shells/{aasIdentifier}/submodels/{submodelIdentifier}/submodel";

        // when
        final String actual = ExtractIdFromProtocolInformation.extractAssetId(href);

        // then
        assertThat(actual).isEqualTo("/shells/{aasIdentifier}/submodels/{submodelIdentifier}/submodel");
    }

}