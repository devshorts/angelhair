package com.godaddy.domains.cassandraqueue.modules;

import com.google.inject.Module;

import java.util.Arrays;
import java.util.List;

public class Modules {
    public static final List<Module> modules =
            Arrays.asList(new DataAccessModule(),
                          new SessionProviderModule());
}
