package com.godaddy.domains.cassandraqueue.modules;

import com.godaddy.domains.cassandraqueue.ServiceConfiguration;
import com.godaddy.domains.common.functional.LazyOne;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

import org.jgroups.JChannel;

public class LeadershipModule extends AbstractModule {

    private LazyOne<ServiceConfiguration, JChannel> lazy = new LazyOne<>(this::createJChannel);

    @Override protected void configure() {

    }

    @Provides
    public JChannel getJChannel(final ServiceConfiguration config) {
        return lazy.get(config);
    }

    private JChannel createJChannel(final ServiceConfiguration config) {
        try {
            return new JChannel(config.getRepairConf().getRaftConfigPath());
        } catch (Exception excn) {
            throw new RuntimeException("Unable to configure JChannel", excn);
        }
    }

}