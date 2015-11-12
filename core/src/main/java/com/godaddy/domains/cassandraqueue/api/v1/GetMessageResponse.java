package com.godaddy.domains.cassandraqueue.api.v1;

import lombok.Value;

@Value
public class GetMessageResponse {
    String popReceipt;
    String message;
}
