package com.godaddy.domains.cassandraqueue.api.v1;

import com.godaddy.domains.cassandraqueue.dataAccess.exceptions.ExistingMonotonFoundException;
import com.godaddy.domains.cassandraqueue.dataAccess.interfaces.QueueRepository;
import com.godaddy.domains.cassandraqueue.factories.MessageRepoFactory;
import com.godaddy.domains.cassandraqueue.factories.MonotonicRepoFactory;
import com.godaddy.domains.cassandraqueue.factories.ReaderFactory;
import com.godaddy.domains.cassandraqueue.model.Message;
import com.godaddy.domains.cassandraqueue.model.PopReceipt;
import com.goddady.cassandra.queue.api.client.GetMessageResponse;
import com.goddady.cassandra.queue.api.client.QueueCreateOptions;
import com.goddady.cassandra.queue.api.client.QueueName;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.inject.Inject;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import lombok.Getter;
import org.joda.time.Duration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
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
    public Response createQueue(@Valid @NotNull QueueCreateOptions createOptions) {
        final QueueName queueName = createOptions.getQueueName();

        try {
            queueRepository.createQueue(queueName);
        }
        catch (Exception e) {
            logger.error(e, "Error");
            return buildErrorResponse("CreateQueue", queueName, e);
        }

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

        if (ensureQueueCreated(queueName)) {
            return buildQueueNotFoundResponse(queueName);
        }

        final Optional<Message> messageOptional;

        try {
            messageOptional = readerFactory.forQueue(queueName)
                                       .nextMessage(Duration.standardSeconds(invisibilityTime));
        }
        catch (Exception e) {
            logger.error(e, "Error");
            return buildErrorResponse("GetMessage", queueName, e);
        }

        if (!messageOptional.isPresent()) {
            return Response.noContent().build();
        }

        final Message messageInstance = messageOptional.get();

        final String popReceipt = PopReceipt.from(messageInstance).toString();

        final String message = messageInstance.getBlob();
        final GetMessageResponse response = new GetMessageResponse(
                popReceipt,
                message,
                messageInstance.getDeliveryCount()
        );

        return Response.ok(response)
                       .status(Response.Status.OK)
                       .build();
    }

    @POST
    @Path("/{queueName}/messages")
    @ApiOperation(value = "Put Message")
    @ApiResponses(value = { @ApiResponse(code = 201, message = "Message Added") })
    public Response putMessage(
            @PathParam("queueName") QueueName queueName,
            @QueryParam("initialInvisibilityTime") @DefaultValue("0") Long initialInvisibilityTime,
            String message) {

        if (ensureQueueCreated(queueName)) {
            return buildQueueNotFoundResponse(queueName);
        }

        try {
            final Message messageToInsert = Message.builder()
                                                   .blob(message)
                                                   .index(monotonicRepoFactory.forQueue(queueName)
                                                                              .nextMonotonic())
                                                   .build();

            final Duration initialInvisibility = Duration.standardSeconds(initialInvisibilityTime);

            messageRepoFactory.forQueue(queueName)
                              .putMessage(messageToInsert,
                                          initialInvisibility);
        }
        catch (ExistingMonotonFoundException e) {
            logger.error(e, "Error");
            return buildErrorResponse("PutMessage", queueName, e);
        }

        return Response.status(Response.Status.CREATED).build();

    }

    @DELETE
    @Path("/{queueName}/messages")
    @ApiOperation(value = "Ack Message")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK") })
    public Response ackMessage(
            @PathParam("queueName") QueueName queueName,
            @QueryParam("popReceipt") String popReceipt) {

        if (ensureQueueCreated(queueName)) {
            return buildQueueNotFoundResponse(queueName);
        }

        boolean messageAcked;

        try {
            messageAcked = readerFactory.forQueue(queueName)
                                        .ackMessage(PopReceipt.valueOf(popReceipt));
        }
        catch (Exception e) {
            logger.error(e, "Error");
            return buildErrorResponse("AckMessage", queueName, e);
        }

        if (messageAcked) {
            return Response.noContent().build();
        }

        return Response.status(Response.Status.CONFLICT).entity("The message is already being processed").build();
    }

    private boolean ensureQueueCreated(final @PathParam("queueName") QueueName queueName) {
        return !queueRepository.queueExists(queueName);
    }

    private Response buildQueueNotFoundResponse(final QueueName queue) {
        return Response.status(Response.Status.NOT_FOUND).entity(new Object() {
            @Getter
            private final String result = "not-found";

            @Getter
            private final String queueName = queue.get();
        }).build();
    }

    private Response buildErrorResponse(final String operation, final QueueName queue, final Exception e) {

        final String errorMessage = e.getMessage();

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(new Object() {
            @Getter
            private final String result = "error";

            @Getter
            private final String op = operation;

            @Getter
            private final QueueName queueName = queue;

            @Getter
            private final String message = errorMessage;
        }).build();
    }
}
