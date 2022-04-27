package net.catenax.irs.systemtest;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Disabled;

@Disabled
public class SystemTestsBase {

    protected static final String PATH_CREATE_JOB = "/irs/jobs";
    protected static final String PATH_GET_JOB = "/irs/jobs/{jobId}";

    protected RequestSpecification getRequestSpecification() {
        var specificationBuilder = new RequestSpecBuilder();
        return specificationBuilder.build();
    }

}
