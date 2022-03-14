package net.catenax.irs.systemtest;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;

/**
 * This class is responsible for running a performance test on connectors integrated with IRS.
 */
@Tag("SystemTests")
public class IrsConnectorPerformanceTest extends SystemTestsBase {

    @Test
    public void test() {
        runGatling(PerformanceTestsRunner.class);
    }

    public static class PerformanceTestsRunner extends IrsConnectorSimulationBase {
        {
            setUp(scenarioBuilder.injectOpen(atOnceUsers(10))).protocols(httpProtocol);
        }
    }
}