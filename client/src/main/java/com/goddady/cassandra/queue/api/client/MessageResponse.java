package com.goddady.cassandra.queue.api.client;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class MessageResponse {
    private String popReceipt;
    private String message;
}
