package com.godaddy.domains.cassandraqueue.model;

public interface MessagePointer extends Pointer {

    default BucketPointer toBucketPointer(int bucketSize){
        final long bucket = get() / bucketSize;

        return () -> bucket;
    }

}
