package org.eclipse.tractusx.irs;

import static io.gatling.javaapi.core.CoreDsl.RawFileBody;
import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

import java.util.HashMap;
import java.util.Map;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

public class IRSLoadTestSimulation extends Simulation {
    {
        final String oauth2_host = System.getenv("OAUTH2_HOST");
        final String clientSecret = System.getenv("OAUTH2_CLIENT_SECRET");
        final String clientId = System.getenv("OAUTH2_CLIENT_ID");
        String body = "grant_type=client_credentials&client_id=" + clientId + "&client_secret=" + clientSecret;
        final String irsUrl = System.getenv("IRS_HOST");

        Map<CharSequence, String> headers_0 = new HashMap<>();
        headers_0.put("Content-Type", "application/x-www-form-urlencoded");

        HttpProtocolBuilder httpProtocol = http.baseUrl(irsUrl)
                                               .acceptHeader("*/*");

        Map<CharSequence, String> headers_1 = new HashMap<>();
        headers_1.put("Authorization", "Bearer #{access_token}");
        headers_1.put("Content-Type", "application/json");

        ScenarioBuilder scn = scenario("IRS Load Test")
                .exec(http("Get access token")
                        .post(oauth2_host)
                        .body(StringBody(body))
                        .asFormUrlEncoded()
                        .headers(headers_0)
                        .check(status().is(200))
                        .check(jsonPath(
                                "$.access_token")
                                .saveAs("access_token")))
                .exec(http("Start Job")
                        .post(irsUrl+"/irs/jobs")
                        .headers(headers_1)
                        .body(RawFileBody(
                                "org/eclipse/tractusx/irs/loadtest/IRS-start-job-body-asset-without-relation.json"))
                        .check(jsonPath("$.id")
                                .saveAs("id")))
                .exec(http("Get Job")
                        .get("/irs/jobs/#{id}?returnUncompletedJob=true")
                        .check(status().is(200))
                        .check(jsonPath(
                                "$..state")
                                .is("RUNNING"))
                        .headers(headers_1));

        setUp(scn.injectOpen(atOnceUsers(10))).protocols(httpProtocol);
    }
}
