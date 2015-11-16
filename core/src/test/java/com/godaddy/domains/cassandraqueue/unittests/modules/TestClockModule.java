package com.godaddy.domains.cassandraqueue.unittests.modules;

import com.godaddy.domains.cassandraqueue.model.Clock;
import com.godaddy.domains.cassandraqueue.modules.ClockModule;
import com.godaddy.domains.cassandraqueue.unittests.time.TestClock;
import com.godaddy.domains.common.test.guice.OverridableModule;
import com.google.inject.Module;

public class TestClockModule extends OverridableModule {

    private final TestClock clock;

    public TestClockModule(TestClock clock) {
        this.clock = clock;
    }

    @Override
    public Class<? extends Module> getOverridesModule() {
        return ClockModule.class;
    }

    @Override
    protected void configure() {
        bind(Clock.class).toInstance(clock);
    }
}
