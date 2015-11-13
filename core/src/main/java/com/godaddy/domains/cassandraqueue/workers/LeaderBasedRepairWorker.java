package com.godaddy.domains.cassandraqueue.workers;

import com.godaddy.domains.cassandraqueue.dataAccess.interfaces.QueueRepository;
import com.godaddy.domains.cassandraqueue.factories.RepairWorkerFactory;
import com.goddady.cassandra.queue.api.client.QueueName;
import com.google.inject.Inject;
import org.jgroups.JChannel;
import org.jgroups.protocols.raft.RAFT;
import org.jgroups.protocols.raft.Role;
import org.jgroups.raft.RaftHandle;

public class LeaderBasedRepairWorker implements RAFT.RoleChange, RepairWorkerManager {

    private static int MEMBER_ID_LAST = 0;

    private final QueueRepository queueRepo;
    private RepairWorkerFactory repairWorkerFactory;
    private RaftHandle raftHandle;

    @Inject
    public LeaderBasedRepairWorker(JChannel jChannel, QueueRepository queueRepo, RepairWorkerFactory repairWorkerFactory) throws Exception {
        raftHandle = new RaftHandle(jChannel, null);
        String raftId = String.valueOf(++MEMBER_ID_LAST);
        raftHandle.raftId(raftId);
        jChannel.connect("raft-cluster");

        this.queueRepo = queueRepo;
        this.repairWorkerFactory = repairWorkerFactory;
    }

    protected void startAll() {
        for (QueueName queueName : queueRepo.getQueues()) {
            repairWorkerFactory.forQueue(queueName).start();
        }
    }

    protected void stopAll() {
        for (QueueName queueName : queueRepo.getQueues()) {
            repairWorkerFactory.forQueue(queueName).stop();
        }
    }

    @Override
    public void roleChanged(Role role) {
        if (role == Role.Leader) {
            startAll();
        } else {
            stopAll();
        }
    }

    @Override public void start() {
        raftHandle.addRoleListener(this);
        if (raftHandle.isLeader()) {
            roleChanged(Role.Leader);
        }
    }

    @Override public void stop() {
        raftHandle.removeRoleListener(this);
        if (raftHandle.isLeader()) {
            roleChanged(Role.Follower);
        }
    }

    public boolean isLeader() {
        return raftHandle.isLeader();
    }
}