package com.godaddy.domains.cassandraqueue.configurations;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

public class RepairConfig {

    @NotEmpty
    @Getter
    @Setter
    private String raftConfigPath = "raft.xml";

}