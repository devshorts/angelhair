package com.godaddy.domains.cassandraqueue.workers;

import lombok.Data;
import org.joda.time.Duration;

@Data
public class BucketConfiguration {
    private final int bucketSize;

    private final Duration repairWorkerTimeout;
}
