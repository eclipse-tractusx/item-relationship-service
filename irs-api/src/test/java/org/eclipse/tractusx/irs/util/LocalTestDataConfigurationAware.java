package org.eclipse.tractusx.irs.util;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.eclipse.tractusx.irs.configuration.local.CxTestDataContainer;
import org.eclipse.tractusx.irs.configuration.local.LocalTestDataConfiguration;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.DeserializationFeature;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

public class LocalTestDataConfigurationAware {

    protected LocalTestDataConfiguration localTestDataConfiguration = mock(LocalTestDataConfiguration.class);

    protected LocalTestDataConfigurationAware() throws IOException {
        final File file = new File("src/main/resources/test_data/CX_Testdata.json");

        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        final CxTestDataContainer cxTestDataContainer = objectMapper.readValue(file, CxTestDataContainer.class);
        when(localTestDataConfiguration.cxTestDataContainer()).thenReturn(cxTestDataContainer);
    }

}
