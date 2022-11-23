package org.eclipse.tractusx.irs.util;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.eclipse.tractusx.irs.configuration.local.CxTestDataContainer;
import org.eclipse.tractusx.irs.configuration.local.LocalTestDataConfiguration;

public class LocalTestDataConfigurationAware {

    protected LocalTestDataConfiguration localTestDataConfiguration = mock(LocalTestDataConfiguration.class);
    protected final ObjectMapper objectMapper = new ObjectMapper();


    protected LocalTestDataConfigurationAware() throws IOException {
        final File file = new File("src/main/resources/test_data/CX_Testdata.json");

        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        final CxTestDataContainer cxTestDataContainer = objectMapper.readValue(file, CxTestDataContainer.class);
        when(localTestDataConfiguration.cxTestDataContainer()).thenReturn(cxTestDataContainer);
    }

}
