package com.godaddy.domains.cassandraqueue.unittests.server;

import com.godaddy.domains.cassandraqueue.ServiceApplication;
import com.godaddy.domains.common.test.guice.ModuleOverrider;
import com.godaddy.domains.common.test.guice.ModuleUtils;
import com.godaddy.domains.common.test.guice.OverridableModule;
import com.google.inject.Module;

import java.util.List;

public class TestService extends ServiceApplication implements ModuleOverrider {
    private List<OverridableModule> modules;

    @Override protected List<Module> getModules() {
        return ModuleUtils.mergeModules(super.getModules(), modules);
    }

    @Override public void overrideModulesWith(final List<OverridableModule> modules) {
        this.modules = modules;
    }

    @Override public List<OverridableModule> getOverrideModules() {
        return modules;
    }
}
