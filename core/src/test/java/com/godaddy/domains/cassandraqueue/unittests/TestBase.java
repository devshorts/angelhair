package com.godaddy.domains.cassandraqueue.unittests;

import ch.qos.logback.classic.Level;
import com.datastax.driver.core.Session;
import com.godaddy.domains.cassandraqueue.ServiceConfiguration;
import com.godaddy.domains.cassandraqueue.configurations.LogMapping;
import com.godaddy.domains.cassandraqueue.dataAccess.interfaces.QueueRepository;
import com.godaddy.domains.cassandraqueue.model.QueueDefinition;
import com.godaddy.domains.cassandraqueue.unittests.time.TestClock;
import com.godaddy.domains.cassandraqueue.unittests.modules.TestClockModule;
import com.goddady.cassandra.queue.api.client.QueueName;
import com.godaddy.domains.cassandraqueue.modules.Modules;
import com.godaddy.domains.cassandraqueue.unittests.modules.InMemorySessionProvider;
import com.godaddy.domains.cassandraqueue.unittests.modules.MockEnvironmentModule;
import com.godaddy.domains.common.test.guice.ModuleUtils;
import com.godaddy.logging.Logger;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.dropwizard.logging.LoggingFactory;
import lombok.AccessLevel;
import lombok.Getter;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static com.godaddy.logging.LoggerFactory.getLogger;

public class TestBase {
    private static final Logger logger = getLogger(TestBase.class);

    public static final Session session;

    static {
        try {
            session = CqlDb.create();
        }
        catch (Exception e) {
            logger.error(e, "Error");

            throw new RuntimeException(e);
        }
    }

    @Getter(AccessLevel.PROTECTED)
    private final TestClock testClock = new TestClock();

    public TestBase() {
        LoggingFactory.bootstrap(Level.ALL);

        LogMapping.register();

        String[] disableLogging = new String[]{ "uk.co.jemos.podam.api.PodamFactoryImpl",
                                                "uk.co.jemos.podam.common.BeanValidationStrategy",
                                                "org.apache.cassandra.service.CassandraDaemon",
                                                "org.apache.cassandra.service.CacheService",
                                                "org.apache.cassandra.db.Memtable",
                                                "org.apache.cassandra.db.ColumnFamilyStore",
                                                "org.apache.cassandra.config.DatabaseDescriptor",
                                                "org.apache.cassandra.db.compaction.CompactionTask",
                                                "org.apache.cassandra.db.DefsTables",
                                                "org.apache.cassandra.service.MigrationManager",
                                                "org.apache.cassandra.config.YamlConfigurationLoader",
                                                "org.apache.cassandra.service.StorageService"
        };

        Arrays.stream(disableLogging).forEach(i -> {
            ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(i)).setLevel(Level.OFF);
        });
    }

    protected Injector getDefaultInjector(ServiceConfiguration configuration) {
        return Guice.createInjector(
                ModuleUtils.mergeModules(Modules.modules,
                                         new InMemorySessionProvider(session),
                                         new MockEnvironmentModule(configuration),
                                         new TestClockModule(testClock)));
    }

    protected Injector getDefaultInjector() {
        return getDefaultInjector(new ServiceConfiguration());
    }

    protected QueueDefinition setupQueue(QueueName queueName) {
        return setupQueue(queueName, 20, getDefaultInjector());
    }

    protected QueueDefinition setupQueue(QueueName queueName, Integer bucketSize) {
        return setupQueue(queueName, bucketSize, getDefaultInjector());
    }

    protected QueueDefinition setupQueue(QueueName queueName, Integer bucketSize, Injector injector) {
        final QueueDefinition queueDefinition = QueueDefinition.builder()
                                                               .queueName(queueName)
                                                               .bucketSize(bucketSize)
                                                               .build();

        createQueue(queueDefinition, injector);
        return queueDefinition;
    }

    private void createQueue(final QueueDefinition queueDefinition, final Injector injector) {
        final QueueRepository queueRepository = injector.getInstance(QueueRepository.class);

        queueRepository.createQueue(queueDefinition);

        queueRepository.getQueue(queueDefinition.getQueueName()).get();
    }

    protected void createQueue(final QueueDefinition queueDefinition) {
        createQueue(queueDefinition, getDefaultInjector());
    }
}
