package net.catenax.prs.smoketest;

import net.catenax.prs.systemtest.SystemTestsBase;
import org.junit.jupiter.api.BeforeEach;

public class SmokeTestsBase extends SystemTestsBase {

    protected static final String PRS_API_LOCALHOST_URI = "http://localhost:8080";
    protected static final String BROKER_PROXY_LOCALHOST_URI = "http://localhost:8081";

    protected String brokerProxyUri;
    protected String prsApiUri;

    @BeforeEach
    public void setUp() {
        // If no config specified, run the smoke test against localhost.
        prsApiUri = System.getProperty("baseURI", PRS_API_LOCALHOST_URI);
        brokerProxyUri = System.getProperty("brokerProxyBaseURI", System.getProperty("baseURI", BROKER_PROXY_LOCALHOST_URI));
    }
}
