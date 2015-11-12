package com.godaddy.domains.cassandraqueue.dataAccess;

import com.godaddy.domains.cassandraqueue.dataAccess.interfaces.RgGatewayRepository;

public class RgGatewayRepositoryImpl implements RgGatewayRepository {
    public String getDomain(String domain){
        return domain;
    }
}
