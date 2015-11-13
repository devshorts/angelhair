package com.godaddy.domains.cassandraqueue.unittests.server;

import com.godaddy.domains.cassandraqueue.ServiceConfiguration;
import com.godaddy.domains.common.test.guice.OverridableModule;
import com.godaddy.domains.common.test.web.runner.ServiceTestRunner;
import com.godaddy.logging.Logger;
import lombok.Getter;

import javax.ws.rs.client.WebTarget;
import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.godaddy.logging.LoggerFactory.getLogger;

public class LiveServer {
    private static final Logger logger = getLogger(LiveServer.class);

    @Getter
    private List<OverridableModule> overridableModules = new ArrayList<>();

    private static Random random = new Random();

    private ServiceTestRunner<ServiceConfiguration, TestService> serviceConfigurationTestServiceServiceTestRunner;

    public void start(ServiceConfiguration configuration) {
        serviceConfigurationTestServiceServiceTestRunner =
                new ServiceTestRunner<>(TestService.class,
                                        getConfig(),
                                        getNextPort());

        serviceConfigurationTestServiceServiceTestRunner.withModules(overridableModules.toArray(new OverridableModule[0])).run();
    }

    public void start(){
        start(getConfig());
    }

    public void stop() throws Exception {
        serviceConfigurationTestServiceServiceTestRunner.close();
    }

    protected static long getNextPort() {
        return random.nextInt(35000) + 15000;
    }

    public WebTarget getClient(String path){
        return serviceConfigurationTestServiceServiceTestRunner.getClient(path);
    }

    public URI getBaseUri(){

        final String uri = String.format("http://localhost:%s", serviceConfigurationTestServiceServiceTestRunner.getLocalPort());

        return URI.create(uri);
    }

    private ServiceConfiguration getConfig() {
        return new ServiceConfiguration();
    }
}
