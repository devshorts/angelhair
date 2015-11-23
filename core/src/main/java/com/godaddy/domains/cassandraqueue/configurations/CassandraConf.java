package com.godaddy.domains.cassandraqueue.configurations;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.datastax.driver.core.Cluster;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.godaddy.domains.cassandraqueue.configurations.config.LoadBalancingPolicy;
import com.godaddy.domains.cassandraqueue.configurations.config.Ssl;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggerFactory;
import com.google.common.base.Strings;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.stuartgunter.dropwizard.cassandra.CassandraFactory;
import org.stuartgunter.dropwizard.cassandra.CassandraHealthCheck;
import org.stuartgunter.dropwizard.cassandra.CassandraMetricSet;

import javax.validation.Valid;
import java.security.NoSuchAlgorithmException;

import static com.codahale.metrics.MetricRegistry.name;

@EqualsAndHashCode(callSuper = false)
@Data
public class CassandraConf extends CassandraFactory {

    private static final Logger logger = LoggerFactory.getLogger(CassandraConf.class);

    @Valid
    @JsonProperty
    private LoadBalancingPolicy loadBalancingPolicy;

    @Valid
    @JsonProperty
    private Ssl ssl;

    /**
     * Builds a {@link com.datastax.driver.core.Cluster} instance.
     *
     * The {@link com.codahale.metrics.MetricRegistry} will be used to register client metrics, and the {@link
     * com.codahale.metrics.health.HealthCheckRegistry} to register client health-checks.
     *
     * @param metrics      the registry to register client metrics.
     * @param healthChecks the registry to register client health-checks.
     * @return a fully configured {@link com.datastax.driver.core.Cluster}.
     */
    public Cluster build(MetricRegistry metrics, HealthCheckRegistry healthChecks) {
        final Cluster.Builder builder = Cluster.builder();
        builder.addContactPoints(getContactPoints());
        builder.withPort(getPort());
        builder.withCompression(getCompression());
        builder.withProtocolVersion(getProtocolVersion());

        if (getQueryOptions() != null) {
            builder.withQueryOptions(getQueryOptions());
        }

        if (getAuthProvider() != null) {
            builder.withAuthProvider(getAuthProvider().build());
        }

        if (getReconnectionPolicy() != null) {
            builder.withReconnectionPolicy(getReconnectionPolicy().build());
        }

        if (getRetryPolicy() != null) {
            builder.withRetryPolicy(getRetryPolicy().build());
        }

        if (getQueryOptions() != null) {
            builder.withQueryOptions(getQueryOptions());
        }

        if (getSocketOptions() != null) {
            builder.withSocketOptions(getSocketOptions());
        }

        if (getPoolingOptions() != null) {
            builder.withPoolingOptions(getPoolingOptions().build());
        }

        if (!isMetricsEnabled()) {
            builder.withoutMetrics();
        }

        if (!isJmxEnabled()) {
            builder.withoutJMXReporting();
        }

        if (!Strings.isNullOrEmpty(getClusterName())) {
            builder.withClusterName(getClusterName());
        }

        if (ssl != null) {
            try {
                builder.withPort(ssl.getPort());
                builder.withSSL(ssl.build());
            }
            catch (NoSuchAlgorithmException e) {
                logger.with(ssl).error("Unable to set SSL");
            }
        }

        if (loadBalancingPolicy != null) {
            builder.withLoadBalancingPolicy(loadBalancingPolicy.build());
        }

        Cluster cluster = builder.build();

        logger.debug("Registering {} Cassandra health check", cluster.getClusterName());
        CassandraHealthCheck healthCheck = new CassandraHealthCheck(cluster, getValidationQuery());
        healthChecks.register(name("cassandra", cluster.getClusterName()), healthCheck);

        if (isMetricsEnabled()) {
            logger.debug("Registering {} Cassandra metrics", cluster.getClusterName());
            metrics.registerAll(new CassandraMetricSet(cluster));
        }

        return cluster;
    }


}
