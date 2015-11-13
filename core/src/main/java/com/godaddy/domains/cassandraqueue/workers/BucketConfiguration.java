package com.godaddy.domains.cassandraqueue.workers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.Duration;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BucketConfiguration {
    private int bucketSize = 1;

    private Duration repairWorkerTimeout = Duration.standardSeconds(3);

    private Duration repairWorkerPollFrequency = Duration.standardSeconds(1);
}
