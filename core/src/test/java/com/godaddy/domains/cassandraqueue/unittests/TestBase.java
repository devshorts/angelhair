package com.godaddy.domains.cassandraqueue.unittests;

import com.datastax.driver.core.Session;
import com.godaddy.domains.cassandraqueue.ServiceConfiguration;
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

    protected Injector getDefaultInjector() {
        return Guice.createInjector(
                ModuleUtils.mergeModules(Modules.modules,
                                         new InMemorySessionProvider(session),
                                         new MockEnvironmentModule<>(new ServiceConfiguration())));
    }
}
