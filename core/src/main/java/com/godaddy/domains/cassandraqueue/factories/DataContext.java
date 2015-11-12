package com.godaddy.domains.cassandraqueue.factories;

import com.godaddy.domains.cassandraqueue.dataAccess.interfaces.MessageRepository;
import com.godaddy.domains.cassandraqueue.dataAccess.interfaces.MonotonicRepository;
import com.godaddy.domains.cassandraqueue.dataAccess.interfaces.PointerRepository;
import lombok.Data;

@Data
public class DataContext{
    private final MessageRepository messageRepository;

    private final MonotonicRepository monotonicRepository;

    private final PointerRepository pointerRepository;
}
