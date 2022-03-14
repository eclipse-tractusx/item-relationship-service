package net.catenax.irs.smoketest;

import net.catenax.irs.systemtest.SystemTestsBase;
import org.junit.jupiter.api.BeforeEach;

public class SmokeTestsBase extends SystemTestsBase {

    protected static final String IRS_API_LOCALHOST_URI = "http://localhost:8080";
    protected static final String BROKER_PROXY_LOCALHOST_URI = "http://localhost:8081";

    protected String brokerProxyUri;
    protected String irsApiUri;

    @BeforeEach
    public void setUp() {
        // If no config specified, run the smoke test against localhost.
        irsApiUri = System.getProperty("baseURI", IRS_API_LOCALHOST_URI);
        brokerProxyUri = System.getProperty("brokerProxyBaseURI", System.getProperty("baseURI", BROKER_PROXY_LOCALHOST_URI));
    }
}
