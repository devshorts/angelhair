package com.godaddy.domains.cassandraqueue.configurations;

import com.godaddy.domains.common.valuetypes.ValueTypeWrapper;
import com.godaddy.logging.LoggingConfigs;
import org.joda.time.DateTime;

import java.net.URI;

public class LogMapping {
    public static void register(){
        LoggingConfigs.getCurrent()
                      .withOverride(URI.class, URI::toString)
                      .withOverride(ValueTypeWrapper.class, ValueTypeWrapper::toString)
                      .withOverride(DateTime.class, DateTime::toString)
                      .withOverride(Class.class, Class::toString);
    }
}
