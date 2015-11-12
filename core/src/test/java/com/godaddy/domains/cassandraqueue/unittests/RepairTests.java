package com.godaddy.domains.cassandraqueue.unittests;

import com.godaddy.domains.cassandraqueue.factories.DataContext;
import com.godaddy.domains.cassandraqueue.factories.DataContextFactory;
import com.godaddy.domains.cassandraqueue.factories.RepairWorkerFactory;
import com.godaddy.domains.cassandraqueue.model.Message;
import com.godaddy.domains.cassandraqueue.model.MonotonicIndex;
import com.godaddy.domains.cassandraqueue.model.QueueName;
import com.godaddy.domains.cassandraqueue.model.ReaderBucketPointer;
import com.godaddy.domains.cassandraqueue.workers.RepairWorker;
import com.google.inject.Injector;
import javafx.util.Duration;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RepairTests extends TestBase {
    @Test
    public void test_repairs() throws InterruptedException {
        final Injector defaultInjector = getDefaultInjector();

        final RepairWorkerFactory repairWorkerFactory = defaultInjector.getInstance(RepairWorkerFactory.class);

        final QueueName queueName = QueueName.valueOf("test_repairs");

        repairWorkerFactory.forQueue(queueName);

        final DataContextFactory contextFactory = defaultInjector.getInstance(DataContextFactory.class);

        final DataContext dataContext = contextFactory.forQueue(queueName);

        final MonotonicIndex index = MonotonicIndex.valueOf(1);

        final Message message = Message.builder()
                                       .blob("BOO!")
                                       .index(index)
                                       .build();

        final RepairWorker repairWorker = repairWorkerFactory.forQueue(queueName);

        repairWorker.start();

        dataContext.getMessageRepository().putMessage(message);

        dataContext.getMessageRepository().tombstone(ReaderBucketPointer.valueOf(0));

        Thread.sleep((long) Duration.seconds(6).toMillis());

        final Message repairedMessage = dataContext.getMessageRepository().getMessage(index);

        assertThat(repairedMessage.isAcked()).isTrue();
    }
}
