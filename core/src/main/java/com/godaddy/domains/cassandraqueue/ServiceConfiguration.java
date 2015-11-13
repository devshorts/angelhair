package com.godaddy.domains.cassandraqueue;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.godaddy.domains.cassandraqueue.configurations.CassandraConf;
import com.godaddy.domains.cassandraqueue.configurations.JerseyConfiguration;
import com.godaddy.domains.cassandraqueue.workers.BucketConfiguration;
import io.dropwizard.Configuration;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class ServiceConfiguration extends Configuration {

    public JerseyConfiguration getJerseyConfiguration() {
        if (jerseyConfiguration == null) {
            return new JerseyConfiguration();
        }

        return jerseyConfiguration;
    }

    @Setter
    private JerseyConfiguration jerseyConfiguration;

    @Valid
    @NotNull
    @JsonProperty("cassandra")
    @Getter
    @Setter
    private CassandraConf cassandraConf;

    @Getter
    @Setter
    private BucketConfiguration bucketConfiguration;
}
