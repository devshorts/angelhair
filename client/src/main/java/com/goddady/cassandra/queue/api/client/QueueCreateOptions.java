package com.goddady.cassandra.queue.api.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class QueueCreateOptions {
    @NotNull
    private final QueueName queueName;

    @JsonCreator
    public QueueCreateOptions(@JsonProperty("queueName") QueueName queueName) {
        this.queueName = queueName;
    }
}
