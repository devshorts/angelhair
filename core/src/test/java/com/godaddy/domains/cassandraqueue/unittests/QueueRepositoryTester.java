package com.godaddy.domains.cassandraqueue.unittests;

import com.godaddy.domains.cassandraqueue.dataAccess.interfaces.QueueRepository;
import com.godaddy.domains.cassandraqueue.model.QueueName;
import com.google.inject.Injector;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class QueueRepositoryTester extends TestBase {
    @Test
    public void queue_operations() throws Exception {
        final Injector defaultInjector = getDefaultInjector();

        final QueueRepository repo = defaultInjector.getInstance(QueueRepository.class);


        final QueueName queueName = QueueName.valueOf("boom");
        assertThat(repo.queueExists(queueName)).isEqualTo(false);

        repo.createQueue(queueName);

        assertThat(repo.queueExists(queueName)).isEqualTo(true);

        assertThat(repo.getQueues().size()).isEqualTo(1);
    }
}
