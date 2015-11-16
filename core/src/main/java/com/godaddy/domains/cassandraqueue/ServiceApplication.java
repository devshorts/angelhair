package com.godaddy.domains.cassandraqueue;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.godaddy.domains.cassandraqueue.configurations.LogMapping;
import com.godaddy.domains.cassandraqueue.handlers.ParameterHandlerProvider;
import com.godaddy.domains.cassandraqueue.modules.Modules;
import com.google.inject.Module;
import com.hubspot.dropwizard.guice.GuiceBundle;
import com.wordnik.swagger.config.ConfigFactory;
import com.wordnik.swagger.config.ScannerFactory;
import com.wordnik.swagger.config.SwaggerConfig;
import com.wordnik.swagger.jaxrs.config.DefaultJaxrsScanner;
import com.wordnik.swagger.jaxrs.listing.ApiDeclarationProvider;
import com.wordnik.swagger.jaxrs.listing.ResourceListingProvider;
import com.wordnik.swagger.jaxrs.reader.DefaultJaxrsApiReader;
import com.wordnik.swagger.jersey.listing.ApiListingResourceJSON;
import com.wordnik.swagger.model.ApiInfo;
import com.wordnik.swagger.reader.ClassReaders;
import de.thomaskrille.dropwizard_template_config.TemplateConfigBundle;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.jersey.jackson.JacksonMessageBodyProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import io.dropwizard.views.ViewRenderer;
import io.dropwizard.views.mustache.MustacheViewRenderer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class ServiceApplication extends Application<ServiceConfiguration> {

    private Environment env;

    public static void main(String[] args) throws Exception {
        new ServiceApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<ServiceConfiguration> bootstrap) {
        bootstrap.addBundle(new TemplateConfigBundle());

        initializeViews(bootstrap);

        initializeDepedencyInjection(bootstrap);
    }

    protected List<Module> getModules() {
        return Modules.modules;
    }

    private void initializeViews(final Bootstrap<ServiceConfiguration> bootstrap) {
        List<ViewRenderer> viewRenders = new ArrayList<>();

        viewRenders.add(new MustacheViewRenderer());

        bootstrap.addBundle(new ViewBundle(viewRenders));

        bootstrap.addBundle(new AssetsBundle("/assets", "/", "index.html"));
    }

    private void initializeDepedencyInjection(final Bootstrap<ServiceConfiguration> bootstrap) {
        final GuiceBundle.Builder<ServiceConfiguration> serviceConfigurationBuilder = GuiceBundle.<ServiceConfiguration>newBuilder();

        getModules().stream().forEach(serviceConfigurationBuilder::addModule);

        GuiceBundle<ServiceConfiguration> guiceBundle = serviceConfigurationBuilder
                .enableAutoConfig(ServiceApplication.class.getPackage().getName())
                .setConfigClass(ServiceConfiguration.class)
                .build();

        bootstrap.addBundle(guiceBundle);
    }

    @Override
    public void run(ServiceConfiguration config, final Environment env) throws Exception {

        this.env = env;

        ArrayList<BiConsumer<ServiceConfiguration, Environment>> run = new ArrayList<>();

        run.add(this::configureJson);

        run.add(this::configureLogging);

        run.add(this::configureDiscoverableApiHelp);

        run.add(this::configureTinyTypeParamterBindings);

        run.stream().forEach(i -> i.accept(config, env));
    }

    public void stop() throws Exception {
        env.getJerseyServletContainer().destroy();
        env.getAdminContext().stop();
    }

    private void configureTinyTypeParamterBindings(final ServiceConfiguration serviceConfiguration, final Environment environment) {
        environment.jersey().register(new ParameterHandlerProvider());
    }

    private void configureJson(ServiceConfiguration config, final Environment environment) {
        ObjectMapper mapper = new JacksonJsonMapper().getMapper();

        JacksonMessageBodyProvider jacksonBodyProvider = new JacksonMessageBodyProvider(mapper, environment.getValidator());

        environment.jersey().register(jacksonBodyProvider);
    }

    private void configureDiscoverableApiHelp(
            final ServiceConfiguration config,
            final Environment environment) {

        environment.jersey().register(new ApiListingResourceJSON());
        environment.jersey().register(new ResourceListingProvider());
        environment.jersey().register(new ApiDeclarationProvider());

        ScannerFactory.setScanner(new DefaultJaxrsScanner());

        ClassReaders.setReader(new DefaultJaxrsApiReader());

        SwaggerConfig swagConfig = ConfigFactory.config();

        swagConfig.setApiVersion("1.0.1");

        swagConfig.setBasePath(environment.getApplicationContext().getContextPath());

        ApiInfo info = new ApiInfo(
                "cassandra-queue API",                             /* title */
                "cassandra-queue API",
                "http://",                  /* TOS URL */
                "domains@godaddy.com",                            /* Contact */
                "Apache 2.0",                                     /* license */
                "http://www.apache.org/licenses/LICENSE-2.0.html" /* license URL */
        );

        swagConfig.setApiInfo(info);
    }

    private void configureLogging(final ServiceConfiguration serviceConfiguration, final Environment environment) {
        LogMapping.register();
    }
}
