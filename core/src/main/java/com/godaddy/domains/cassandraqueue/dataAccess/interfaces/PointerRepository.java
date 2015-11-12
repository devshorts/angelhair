package com.godaddy.domains.cassandraqueue.dataAccess.interfaces;

import com.godaddy.domains.cassandraqueue.model.InvisibilityMessagePointer;
import com.godaddy.domains.cassandraqueue.model.ReaderBucketPointer;
import com.godaddy.domains.cassandraqueue.model.RepairBucketPointer;

public interface PointerRepository {
    /**
     * Conditional update the bucket if the message pointer still points to pointer
     *
     * otherwise return the value in the pointer. IF for wahtever reason they are still the same (weird)
     * try again
     *
     * @param ptr
     * @return
     */
    ReaderBucketPointer advanceMessageBucketPointer(ReaderBucketPointer original, ReaderBucketPointer ne);

    /**
     * Conditional update of either the minimum of the current in the db or the destination
     *
     * @param destination
     */
    InvisibilityMessagePointer moveInvisiblityPointerTo(InvisibilityMessagePointer original, InvisibilityMessagePointer destination);

    RepairBucketPointer advanceRepairBucketPointer(RepairBucketPointer original, RepairBucketPointer next);

    InvisibilityMessagePointer getCurrentInvisPointer();

    ReaderBucketPointer getReaderCurrentBucket();

    RepairBucketPointer getRepairCurrentBucketPointer();
}
