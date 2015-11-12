package com.godaddy.domains.cassandraqueue.dataAccess.interfaces;

import com.godaddy.domains.cassandraqueue.model.BucketPointer;
import com.godaddy.domains.cassandraqueue.model.InvisibilityMessagePointer;

public interface PointerRepository {
    void moveMessagePointerTo(BucketPointer ptr);

    void moveInvisiblityPointerTo(InvisibilityMessagePointer ptr);
}
