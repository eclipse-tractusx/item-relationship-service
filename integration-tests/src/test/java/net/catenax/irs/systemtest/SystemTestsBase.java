package net.catenax.irs.systemtest;

import io.gatling.app.Gatling;
import io.gatling.core.config.GatlingPropertiesBuilder;
import io.gatling.javaapi.core.Simulation;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Disabled;

@Disabled
public class SystemTestsBase {

    protected static final String PATH_BY_VIN = "/api/v0.1/vins/{vin}/partsTree";
    protected static final String SAMPLE_VIN = "YS3DD78N4X7055320";
    protected static final String VIN = "vin";
    protected static final String VIEW = "view";

    protected RequestSpecification getRequestSpecification() {
        var specificationBuilder = new RequestSpecBuilder();
        return specificationBuilder.build();
    }

    protected void runGatling(Class<? extends Simulation> simulation) {
        var props = new GatlingPropertiesBuilder();
        props.simulationClass(simulation.getCanonicalName());
        props.resultsDirectory("target/gatling");
        Gatling.fromMap(props.build());
    }

}
