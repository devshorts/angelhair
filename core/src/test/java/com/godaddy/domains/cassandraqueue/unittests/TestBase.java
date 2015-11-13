package com.godaddy.domains.cassandraqueue.unittests;

import com.datastax.driver.core.Session;
import com.godaddy.domains.cassandraqueue.ServiceConfiguration;
import com.godaddy.domains.cassandraqueue.model.MonotonicIndex;
import com.godaddy.domains.cassandraqueue.modules.Modules;
import com.godaddy.domains.cassandraqueue.unittests.modules.InMemorySessionProvider;
import com.godaddy.domains.cassandraqueue.unittests.modules.MockEnvironmentModule;
import com.godaddy.domains.common.test.guice.ModuleUtils;
import com.godaddy.logging.Logger;
import com.google.inject.Guice;
import com.google.inject.Injector;

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

    protected Injector getDefaultInjector(ServiceConfiguration configuration) {
        return Guice.createInjector(
                ModuleUtils.mergeModules(Modules.modules,
                                         new InMemorySessionProvider(session),
                                         new MockEnvironmentModule<>(configuration)));
    }

    protected Injector getDefaultInjector(){
        return getDefaultInjector(new ServiceConfiguration());
    }

    private static int counter = 0;

    protected int getNextIntForTesting() {
        return ++counter;
    }

    protected MonotonicIndex getTestMonoton(){
        return MonotonicIndex.valueOf(getNextIntForTesting());
    }
}
