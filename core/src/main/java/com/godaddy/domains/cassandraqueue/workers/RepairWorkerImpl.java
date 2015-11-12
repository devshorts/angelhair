package com.godaddy.domains.cassandraqueue.workers;

import com.godaddy.domains.cassandraqueue.dataAccess.interfaces.MessageRepository;
import com.godaddy.domains.cassandraqueue.dataAccess.interfaces.PointerRepository;
import com.godaddy.domains.cassandraqueue.model.ReaderBucketPointer;
import com.godaddy.domains.cassandraqueue.model.MonotonicIndex;
import com.google.inject.Inject;

public class RepairWorkerImpl implements RepairWorker {
    private final PointerRepository repository;
    private final MessageRepository messageRepository;

    @Inject
    public RepairWorkerImpl(PointerRepository repository, MessageRepository messageRepository){
        this.repository = repository;
        this.messageRepository = messageRepository;
    }

    @Override public void start() {

    }

    private ReaderBucketPointer getCurrentBucket(){
        return null;
    }

    private ReaderBucketPointer findFirstBucketToMonitor(){
        // first bucket that is tombstoned and is unfilled

        return null;
    }

    private void republishMessage(MonotonicIndex index){
        // if a message showed up in a tombstoned bucket,
        //      republish
        //      ack origin
    }
}
