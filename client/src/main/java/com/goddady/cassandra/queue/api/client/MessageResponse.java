package com.goddady.cassandra.queue.api.client;

import lombok.Data;

@Data
public class MessageResponse {
    private String popReceipt;
    private String message;
}
