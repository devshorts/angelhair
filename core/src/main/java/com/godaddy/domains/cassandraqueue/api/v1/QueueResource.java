package com.godaddy.domains.cassandraqueue.api.v1;

import com.godaddy.domains.cassandraqueue.dataAccess.interfaces.QueueRepository;
import com.godaddy.domains.cassandraqueue.factories.MessageRepoFactory;
import com.godaddy.domains.cassandraqueue.factories.MonotonicRepoFactory;
import com.godaddy.domains.cassandraqueue.factories.ReaderFactory;
import com.godaddy.domains.cassandraqueue.model.Message;
import com.godaddy.domains.cassandraqueue.model.PopReceipt;
import com.godaddy.domains.cassandraqueue.model.QueueName;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.inject.Inject;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import lombok.Getter;
import org.joda.time.Duration;

import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Optional;

@Path("/v1/queues")
@Api(value = "/v1/queues", description = "Queue api")
@Produces(MediaType.APPLICATION_JSON)
public class QueueResource {

    private static final Logger logger = LoggerFactory.getLogger(QueueResource.class);
    private final ReaderFactory readerFactory;
    private final MessageRepoFactory messageRepoFactory;
    private final MonotonicRepoFactory monotonicRepoFactory;
    private final QueueRepository queueRepository;


    @Inject
    public QueueResource(
            ReaderFactory readerFactory,
            MessageRepoFactory messageRepoFactory,
            MonotonicRepoFactory monotonicRepoFactory,
            QueueRepository queueRepository) {
        this.readerFactory = readerFactory;
        this.messageRepoFactory = messageRepoFactory;
        this.monotonicRepoFactory = monotonicRepoFactory;
        this.queueRepository = queueRepository;
    }

    @POST
    @Path("/")
    @ApiOperation(value = "Create Queue")
    @ApiResponses(value = { @ApiResponse(code = 201, message = "Created") })
    public Response createQueue(QueueName queueName) {

        queueRepository.createQueue(queueName);

        return Response.ok().status(Response.Status.CREATED).build();
    }

    @GET
    @Path("/{queueName}/messages/next")
    @ApiOperation(value = "Get Message")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 204, message = "No message")
    })
    public Response getMessage(
            @PathParam("queueName") QueueName queueName,
            @QueryParam("invisibilityTime") @DefaultValue("30") Long invisibilityTime) {

        if(!queueRepository.queueExists(queueName)){
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        final Optional<Message> nextMessage = readerFactory.forQueue(queueName)
                                                           .nextMessage(Duration.standardSeconds(invisibilityTime));

        if (!nextMessage.isPresent()) {
            return Response.noContent().build();
        }

        final String popReceipt = PopReceipt.from(nextMessage.get()).toString();

        final String message = nextMessage.get().getBlob();
        final GetMessageResponse response = new GetMessageResponse(
                popReceipt,
                message
        );

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
            @QueryParam("initialInvisibilityTime") @DefaultValue("0") Long initialInvisibilityTime,
            String message) {

        if(!queueRepository.queueExists(queueName)){
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        try {
            messageRepoFactory.forQueue(queueName)
                              .putMessage(Message.builder()
                                                 .blob(message)
                                                 .index(monotonicRepoFactory.forQueue(queueName)
                                                                            .nextMonotonic())
                                                 .build(),
                                          Duration.standardSeconds(initialInvisibilityTime));

            return Response.noContent().build();
        }
        catch (Exception e) {
            logger.error(e, "error putting message");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new Object() {
                @Getter
                String message = e.getMessage();
            }).build();
        }
    }

    @DELETE
    @Path("/{queueName}/messages")
    @ApiOperation(value = "Ack Message")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK") })
    public Response ackMessage(
            @PathParam("queueName") QueueName queueName,
            @QueryParam("popReceipt") String popReceipt) {

        if(!queueRepository.queueExists(queueName)){
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        boolean messageAcked = readerFactory.forQueue(queueName)
                                            .ackMessage(PopReceipt.valueOf(popReceipt));

        if (messageAcked) {
            return Response.noContent().build();
        }

        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
