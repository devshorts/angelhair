package com.godaddy.domains.cassandraqueue.modules;

import com.godaddy.domains.cassandraqueue.ServiceConfiguration;
import com.godaddy.domains.cassandraqueue.workers.BucketConfiguration;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class ConfigProviderModule extends AbstractModule {
    @Override protected void configure() {

    }

    @Provides
    public BucketConfiguration bucketConfig(ServiceConfiguration config) {
        return config.getBucketConfiguration();
    }
}
