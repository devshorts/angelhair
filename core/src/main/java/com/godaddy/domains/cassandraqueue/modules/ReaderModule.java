package com.godaddy.domains.cassandraqueue.modules;

import com.godaddy.domains.cassandraqueue.factories.ReaderFactory;
import com.godaddy.domains.cassandraqueue.model.Clock;
import com.godaddy.domains.cassandraqueue.model.JodaClock;
import com.godaddy.domains.cassandraqueue.workers.Reader;
import com.godaddy.domains.cassandraqueue.workers.ReaderImpl;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class ReaderModule extends AbstractModule {
    @Override protected void configure() {
        install(new FactoryModuleBuilder()
                        .implement(Reader.class, ReaderImpl.class)
                        .build(ReaderFactory.class));
    }
}

