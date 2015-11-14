package com.godaddy.domains.cassandraqueue.unittests;

import com.godaddy.domains.cassandraqueue.dataAccess.interfaces.QueueRepository;
import com.godaddy.domains.cassandraqueue.model.QueueDefinition;
import com.goddady.cassandra.queue.api.client.QueueName;
import com.google.inject.Injector;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class QueueRepositoryTester extends TestBase {
    @Test
    public void queue_operations() throws Exception {
        final Injector defaultInjector = getDefaultInjector();

        final QueueRepository repo = defaultInjector.getInstance(QueueRepository.class);

        final QueueName queueName = QueueName.valueOf("queue_operations");

        assertThat(repo.queueExists(queueName)).isEqualTo(false);

        final QueueDefinition queueDefinition = QueueDefinition.builder().queueName(queueName).build();

        repo.createQueue(queueDefinition);

        assertThat(repo.queueExists(queueName)).isEqualTo(true);

        assertThat(repo.getQueueNames()).contains(queueName);
    }
}
