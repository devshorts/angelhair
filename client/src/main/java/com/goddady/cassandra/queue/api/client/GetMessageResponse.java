package com.goddady.cassandra.queue.api.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class GetMessageResponse {
    String popReceipt;
    String message;
    int deliveryCount;

    @JsonCreator
    public GetMessageResponse(
            @JsonProperty("popReceipt") String popReceipt,
            @JsonProperty("message") String message,
            @JsonProperty("deliveryCount") int deliveryCount) {

        this.popReceipt = popReceipt;
        this.message = message;
        this.deliveryCount = deliveryCount;
    }
}
