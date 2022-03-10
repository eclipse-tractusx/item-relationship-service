package net.catenax.prs.systemtest;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.gatling.javaapi.core.CoreDsl.incrementUsersPerSec;

/**
 * This class is responsible for running stress tests on connectors integrated with PRS.
 */
@Tag("StressTests")
public class PrsConnectorStressTest extends SystemTestsBase {

    @Test
    public void test() {
        runGatling(StressTestsRunner.class);
    }

    public static class StressTestsRunner extends PrsConnectorSimulationBase {

        {
            this.vehicleOneId = "CAXSWPFTJQEVZNZZ";
            this.vehicleObjectId = "OXCNTJT4D6AWSGAK3";
            this.depth = 5;

            // generate an open workload injection profile
            // with levels of 10, 15, 20, 25 and 30 arriving users per second
            // separated by linear ramps lasting 10 seconds
            // each level lasting 10 seconds
            // triggers part tree request 1000 times during all levels and 800 during ramps
            setUp(scenarioBuilder.injectOpen(
                    incrementUsersPerSec(5)
                            .times(5)
                            .eachLevelLasting(10)
                            .separatedByRampsLasting(10)
                            .startingFrom(10))).protocols(httpProtocol);
        }
    }
}