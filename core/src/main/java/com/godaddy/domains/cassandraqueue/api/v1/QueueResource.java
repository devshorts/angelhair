package com.godaddy.domains.cassandraqueue.api.v1;

import com.godaddy.domains.cassandraqueue.model.QueueName;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import org.joda.time.Duration;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/v1/queues")
@Api(value = "/v1/queues", description = "Queue api")
@Produces(MediaType.APPLICATION_JSON)
public class QueueResource {

    private static final Logger logger = LoggerFactory.getLogger(QueueResource.class);

    @GET
    @Path("/{queueName}/messages/next")
    @ApiOperation(value = "Get Message")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK") })
    public Response getMessage(
            @PathParam("queueName") QueueName queueName,
            @QueryParam("invisibilityTime") Duration invisibilityTime) {

        // Need to get pop receipt + message


        Object response = new Object() {

        };

        return Response.ok(response)
                       .status(Response.Status.OK)
                       .build();
    }

    @POST
    @Path("/{queueName}/messages")
    @ApiOperation(value = "Put Message")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK") })
    public Response putMessage(
            @PathParam("queueName") QueueName queueName,
            @QueryParam("initialInvisibilityTime") Duration initialInvisibilityTime,
            String message) {

        // Put message, initially invisible for initialInvisibilityTime


        Object response = new Object() {

        };

        return Response.ok(response)
                       .status(Response.Status.OK)
                       .build();
    }

    @DELETE
    @Path("/{queueName}/messages")
    @ApiOperation(value = "Ack Message")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK") })
    public Response ackMessage(
            @PathParam("queueName") QueueName queueName,
            @QueryParam("popReceipt") String popReceipt) {

        // Put message, initially invisible for initialInvisibilityTime


        Object response = new Object() {

        };

        return Response.ok(response)
                       .status(Response.Status.OK)
                       .build();
    }
}
