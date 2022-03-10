//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.prs.connector.consumer.controller;


import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import net.catenax.prs.connector.consumer.middleware.RequestMiddleware;
import net.catenax.prs.connector.consumer.service.ConsumerService;
import net.catenax.prs.connector.consumer.service.StatusResponse;
import net.catenax.prs.connector.parameters.GetStatusParameters;
import net.catenax.prs.connector.requests.PartsTreeRequest;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;

/**
 * Consumer API Controller.
 * Provides consumer extra endpoints.
 */
@Consumes({MediaType.APPLICATION_JSON})
@Produces({MediaType.APPLICATION_JSON})
@Path(ConsumerApiController.API_VERSION)
@RequiredArgsConstructor
public class ConsumerApiController {

    /**
     * The API version, part of the URL.
     */
    public static final String API_VERSION = "v0.1";

    /**
     * Logger.
     */
    private final Monitor monitor;

    /**
     * Service implementation.
     */
    private final ConsumerService service;

    /**
     * Middleware to process request exceptions.
     */
    private final RequestMiddleware middleware;

    /**
     * Health endpoint.
     *
     * @return Consumer status
     */
    @GET
    @Path("health")
    public String checkHealth() {
        monitor.info("Received a health request");
        return "I'm alive!";
    }

    /**
     * Endpoint to trigger a request, so that parts tree get written into a blob.
     * Consumer will forward the PartsTreeByObjectIdRequest to providers.
     *
     * @param request Contains a PartsTreeByObjectIdRequest corresponding to prs-request and other
     *                information such that the destination file where the result of the PRS
     *                request should be written.
     * @return Response with job id.
     */
    @POST
    @Path("retrievePartsTree")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response retrievePartsTree(final PartsTreeRequest request) {
        return middleware.chain()
                .validate(request)
                .invoke(() -> {
                    final var jobInfo = service.retrievePartsTree(request);
                    return Response.ok(jobInfo.getJobId()).build();
                });
    }

    /**
     * Provides status of a job
     *
     * @param parameters request parameters
     * @return Job state
     */
    @GET
    @Path("datarequest/{id}/state")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getStatus(final @BeanParam GetStatusParameters parameters) {
        return middleware.chain()
                .validate(parameters)
                .invoke(() -> {
                    final var status = service.getStatus(parameters.getRequestId());
                    if (status.isEmpty()) {
                        return Response.status(Response.Status.NOT_FOUND).build();
                    }
                    final StatusResponse statusResponse = status.get();
                    if (statusResponse.getSasToken() != null) {
                        return Response.ok(statusResponse.getSasToken()).build();
                    }
                    return Response.accepted(statusResponse.getStatus().toString()).build();
                });
    }
}
