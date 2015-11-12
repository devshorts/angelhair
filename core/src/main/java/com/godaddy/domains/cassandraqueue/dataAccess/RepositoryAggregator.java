package com.godaddy.domains.cassandraqueue.dataAccess;

import com.godaddy.domains.cassandraqueue.dataAccess.interfaces.RgGatewayRepository;
import com.google.inject.Inject;
import lombok.Getter;

public class RepositoryAggregator {

    @Getter
    private final RgGatewayRepository rgGateway;

    @Inject
    public RepositoryAggregator(RgGatewayRepository rgGateway) {
        this.rgGateway = rgGateway;
    }
}
