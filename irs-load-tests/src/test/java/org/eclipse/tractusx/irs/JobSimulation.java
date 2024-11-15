package org.eclipse.tractusx.irs;

import static io.gatling.javaapi.core.CoreDsl.RawFileBody;
import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

//mvn gatling:test -pl irs-load-tests -Dgatling.simulationClass=org.eclipse.tractusx.irs.JobSimulation
public class JobSimulation extends Simulation {

    private static final String BASE_URL = System.getenv("IRS_HOST");
    private static final Boolean ASSETS_WITH_CHILDS = Boolean.valueOf(System.getenv("ASSETS_WITH_CHILDS"));
    private static final String X_API_KEY = System.getenv("IRS_ADMIN_API_KEY");
    private static final Integer SCENARIO_DURATION = Integer.valueOf(System.getenv("SCENARIO_DURATION"));

    HttpProtocolBuilder httpProtocol = http.baseUrl(BASE_URL).acceptHeader("application/json").contentTypeHeader("application/json");

    ScenarioBuilder scenario = scenario("Creating Jobs")
            .exec(http("Start Job").post("/irs/jobs")
                                   .header("X-API-KEY", X_API_KEY)
                                   .body(RawFileBody(
                                           getRequestBody()))
                                   .check(status().is(201))
                                   .check(jsonPath("$.id").saveAs("id")))
            .doWhile(session -> !Objects.equals(session.getString("state"), "COMPLETED"))
            .on(
                    exec(session -> {
                        System.out.println("id: " + session.getString("id") + ", status: "+ session.getString("state"));
                        return session;
                    }).
                              exec(
                              http("Check state Request")
                                      .get("/irs/jobs/#{id}")
                                      .header("X-API-KEY", X_API_KEY)
                                      .check(status().in(206, 200))
                                      .check(jsonPath("$.job.state").saveAs("state"))
                                      .check(jsonPath("$.job.startedOn").saveAs("startedOn"))
                                      .check(jsonPath("$.job.completedOn").saveAs("completedOn"))
                      )
                      .pause(3)
            ).exec(
                    session -> {
                        final String startedOn = session.getString("startedOn");
                        final String completedOn = session.getString("completedOn");
                        Instant start = Instant.parse(startedOn);
                        Instant end = Instant.parse(completedOn);
                        Duration duration = Duration.between(start, end);

                        writeToFile(session.getString("id"), duration.toMillis());
                        System.out.println("Duration in seconds: " + duration.toSeconds());
                        System.out.println("Duration in milliseconds: " + duration.toMillis());
                        return session;
                    }
            );

    {
        setUp(scenario.injectOpen(constantUsersPerSec(1).during(SCENARIO_DURATION)).protocols(httpProtocol));
    }

    private void writeToFile(final String jobId, final long durationMillis) {
        String csvFile = "irs-load-tests/target/job-duration.csv";
        try (FileWriter writer = new FileWriter(csvFile, true)) {
            writer.append("\"").append(jobId).append("\",").append(String.valueOf(durationMillis)).append("\n");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private String getRequestBody(){
        if(ASSETS_WITH_CHILDS){
            return "org/eclipse/tractusx/irs/loadtest/IRS-start-job-body-asset-with-child-relations.json";
        } else {
            return "org/eclipse/tractusx/irs/loadtest/IRS-start-job-body-asset-without-relation.json";
        }
    }

}

