package com.godaddy.domains.cassandraqueue;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.godaddy.domains.cassandraqueue.configurations.CassandraConf;
import com.godaddy.domains.cassandraqueue.configurations.JerseyConfiguration;
import com.godaddy.domains.cassandraqueue.configurations.ServerConfig;
import com.godaddy.domains.cassandraqueue.workers.BucketConfiguration;
import com.godaddy.domains.cassandraqueue.configurations.RepairConfig;
import io.dropwizard.Configuration;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class ServiceConfiguration extends Configuration {

    @Getter
    @JsonProperty("jersey")
    @Setter
    private JerseyConfiguration jerseyConfiguration = new JerseyConfiguration();

    @Getter
    @Setter
    @JsonProperty("bucket")
    private BucketConfiguration bucketConfiguration = new BucketConfiguration();

    @Valid
    @NotNull
    @JsonProperty("cassandra")
    @Getter
    @Setter
    private CassandraConf cassandraConf = new CassandraConf();

    @Valid
    @NotNull
    @JsonProperty("repair")
    @Getter
    @Setter
    private RepairConfig repairConf = new RepairConfig();

    @Valid
    @NotNull
    @JsonProperty("server")
    @Getter
    @Setter
    private ServerConfig serverConf = new ServerConfig();

}
