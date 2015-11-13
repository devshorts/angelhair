package com.godaddy.domains.cassandraqueue;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.godaddy.domains.cassandraqueue.configurations.CassandraConf;
import com.godaddy.domains.cassandraqueue.configurations.JerseyConfiguration;
<<<<<<< HEAD
import com.godaddy.domains.cassandraqueue.workers.BucketConfiguration;
=======
import com.godaddy.domains.cassandraqueue.configurations.RepairConfig;
>>>>>>> f906fbd03794688cc1d322459cb24de144d945d0
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
    @JsonProperty("bucket")
    private BucketConfiguration bucketConfiguration;

    @Valid
    @NotNull
    @JsonProperty("repair")
    @Getter
    @Setter
    private RepairConfig repairConf = new RepairConfig();
}
