package com.godaddy.domains.cassandraqueue.unittests.modules;

import com.godaddy.domains.common.test.guice.OverridableModule;
import com.google.inject.Module;
import io.dropwizard.setup.Environment;
import lombok.Getter;
import org.mockito.Mockito;

/**
 * Can be used to mock out dropwizard environment and auto mock the config class for a dropwizard service
 */
public class MockEnvironmentModule<T> extends OverridableModule {
    private final Class<T> configClass;

    @Getter
    private final T configInstance;

    @Getter private Environment mockEnvironment = Mockito.mock(Environment.class);


    public MockEnvironmentModule(Class<T> configClass) {
        this.configClass = configClass;

        configInstance = Mockito.mock(configClass);
    }

    public MockEnvironmentModule(T config) {
        configClass = (Class<T>) config.getClass();

        configInstance = config;
    }

    @Override public Class<? extends Module> getOverridesModule() {
        return null;
    }

    @Override protected void configure() {
        bind(Environment.class).toInstance(mockEnvironment);

        bind(configClass).toInstance(configInstance);
    }
}