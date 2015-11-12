package com.godaddy.domains.cassandraqueue.dataAccess.interfaces;

import com.godaddy.domains.cassandraqueue.model.BucketPointer;
import com.godaddy.domains.cassandraqueue.model.InvisibilityMessagePointer;
import com.godaddy.domains.cassandraqueue.model.MonotonicIndex;

public interface PointerRepository {
    /**
     * Conditional update teh bucket if the message pointer still points to pointer
     *
     * otherwise return the value in the pointer. IF for wahtever reason they are still the same (weird)
     * try again
     * @param ptr
     * @return
     */
    BucketPointer advanceMessageBucketPointer(BucketPointer ptr);

    /**
     * Conditional update of either the minimum of the current in the db or the destination
     * @param destination
     */
    void moveInvisiblityPointerTo(MonotonicIndex destination);

    InvisibilityMessagePointer getCurrentInvisPointer();

    void moveMessagePointerTo(BucketPointer ptr);

    void moveInvisiblityPointerTo(InvisibilityMessagePointer ptr);

    BucketPointer getReaderCurrentBucket();
}
