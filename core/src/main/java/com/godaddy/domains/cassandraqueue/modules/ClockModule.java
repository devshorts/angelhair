package com.godaddy.domains.cassandraqueue.modules;

import com.godaddy.domains.cassandraqueue.model.Clock;
import com.godaddy.domains.cassandraqueue.model.JodaClock;
import com.google.inject.AbstractModule;

public class ClockModule extends AbstractModule {
    @Override protected void configure() {
        bind(Clock.class).to(JodaClock.class);
    }
}
