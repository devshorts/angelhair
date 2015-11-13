package com.godaddy.domains.cassandraqueue.modules;

import com.godaddy.domains.cassandraqueue.factories.RepairWorkerFactory;
import com.godaddy.domains.cassandraqueue.modules.annotations.RepairPool;
import com.godaddy.domains.cassandraqueue.workers.RepairWorker;
import com.godaddy.domains.cassandraqueue.workers.RepairWorkerImpl;
import com.godaddy.domains.cassandraqueue.workers.RepairWorkerManager;
import com.godaddy.domains.cassandraqueue.workers.SimpleRepairWorkerManager;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class RepairWorkerModule extends AbstractModule {

    @Override protected void configure() {
        install(new FactoryModuleBuilder()
                        .implement(RepairWorker.class, RepairWorkerImpl.class)
                        .build(RepairWorkerFactory.class));

        bind(RepairWorkerManager.class).to(SimpleRepairWorkerManager.class).in(Singleton.class);
    }

    @Singleton
    @RepairPool
    @Provides
    public ScheduledExecutorService executorService(){
        return Executors.newScheduledThreadPool(10);
    }
}
