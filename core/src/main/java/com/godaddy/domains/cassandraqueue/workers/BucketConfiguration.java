package com.godaddy.domains.cassandraqueue.workers;

import lombok.Data;

@Data
public class BucketConfiguration {
    private final int bucketSize;
}
