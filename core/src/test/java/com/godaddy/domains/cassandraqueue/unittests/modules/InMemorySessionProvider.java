package com.godaddy.domains.cassandraqueue.unittests.modules;

import com.datastax.driver.core.Session;
import com.godaddy.domains.cassandraqueue.modules.SessionProviderModule;
import com.godaddy.domains.common.test.guice.OverridableModule;
import com.google.inject.Module;

public class InMemorySessionProvider extends OverridableModule {
    private final Session session;

    public InMemorySessionProvider(Session session) {
        this.session = session;
    }

    @Override public Class<? extends Module> getOverridesModule() {
        return SessionProviderModule.class;
    }

    @Override protected void configure() {
        bind(Session.class).toInstance(session);
    }
}
