package com.godaddy.domains.cassandraqueue.unittests;

import com.datastax.driver.core.Session;
import com.godaddy.domains.common.test.db.CqlUnitDb;

public class CqlDb {
    public static Session create() throws Exception {
        return CqlUnitDb.create("../db/scripts");
    }
}