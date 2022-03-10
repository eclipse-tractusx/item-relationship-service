package net.catenax.prs.systemtest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import net.catenax.prs.connector.requests.PartsTreeByObjectIdRequest;
import net.catenax.prs.connector.requests.PartsTreeRequest;
import net.catenax.prs.dtos.PartsTreeView;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.bodyString;
import static io.gatling.javaapi.core.CoreDsl.doWhileDuring;
import static io.gatling.javaapi.core.CoreDsl.exec;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class PrsConnectorSimulationBase extends Simulation {

    private static final String connectorUri = System.getenv()
            .getOrDefault("ConnectorURI", "https://catenaxdev001akssrv.germanywestcentral.cloudapp.azure.com/prs-connector-consumer/api/v0.1");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    protected HttpProtocolBuilder httpProtocol = http.baseUrl(connectorUri)
            .acceptHeader("*/*").contentTypeHeader("application/json");
    // Trigger a get parts tree request. Then call status endpoint every second till it returns 200.

    protected String vehicleOneId = "CAXSWPFTJQEVZNZZ";
    protected String vehicleObjectId = "UVVZI9PKX5D37RFUB";
    protected int depth = 2;

    protected ScenarioBuilder scenarioBuilder = scenario("Trigger Get parts tree for a part.")
            .repeat(1)
            .on(exec(
                            http("Trigger partsTree request")
                                    .post("/retrievePartsTree")
                                    .body(StringBody(getSerializedPartsTreeRequest()))
                                    .check(status().is(200)).check(bodyString().saveAs("requestId"))
                    )
                    // Call status endpoint every second, till it gives a 200 status code.
                    .exec(session -> session.set("status", -1))
                    .group("waitForCompletion").on(
                            doWhileDuring(session -> session.getInt("status") != 200, Duration.ofSeconds(12))
                                    .on(exec(http("Get status")
                                                    .get(session -> String.format("/datarequest/%s/state", session.getString("requestId")))
                                                    .check(status().saveAs("status")))
                                            .pause(Duration.ofSeconds(1)))));

    protected String getSerializedPartsTreeRequest() {
        var params = PartsTreeRequest.builder()
                .byObjectIdRequest(
                        PartsTreeByObjectIdRequest.builder()
                                .oneIDManufacturer(vehicleOneId)
                                .objectIDManufacturer(vehicleObjectId)
                                .view(PartsTreeView.AS_BUILT.name())
                                .aspect(PrsConnectorStressTest.ASPECT_MATERIAL)
                                .depth(depth)
                                .build()).build();

        try {
            return MAPPER.writeValueAsString(params);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Exception serializing parts tree request", e);
        }
    }
}