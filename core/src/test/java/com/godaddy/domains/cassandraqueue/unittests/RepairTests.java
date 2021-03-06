package com.godaddy.domains.cassandraqueue.unittests;

import com.godaddy.domains.cassandraqueue.ServiceConfiguration;
import com.godaddy.domains.cassandraqueue.dataAccess.exceptions.ExistingMonotonFoundException;
import com.godaddy.domains.cassandraqueue.factories.DataContext;
import com.godaddy.domains.cassandraqueue.factories.DataContextFactory;
import com.godaddy.domains.cassandraqueue.factories.RepairWorkerFactory;
import com.godaddy.domains.cassandraqueue.model.Message;
import com.godaddy.domains.cassandraqueue.model.MonotonicIndex;
import com.godaddy.domains.cassandraqueue.model.QueueDefinition;
import com.godaddy.domains.cassandraqueue.workers.RepairWorkerImpl;
import com.goddady.cassandra.queue.api.client.MessageTag;
import com.goddady.cassandra.queue.api.client.QueueName;
import com.godaddy.domains.cassandraqueue.model.ReaderBucketPointer;
import com.godaddy.domains.cassandraqueue.model.RepairBucketPointer;
import com.godaddy.domains.cassandraqueue.workers.BucketConfiguration;
import com.google.inject.Injector;
import org.joda.time.Duration;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

public class RepairTests extends TestBase {
    @Test
    public void repairer_republishes_newly_visible_in_tombstoned_bucket() throws InterruptedException, ExistingMonotonFoundException, ExecutionException {

        final ServiceConfiguration serviceConfiguration = new ServiceConfiguration();

        final BucketConfiguration bucketConfiguration = new BucketConfiguration();

        bucketConfiguration.setRepairWorkerTimeout(Duration.standardSeconds(3));

        serviceConfiguration.setBucketConfiguration(bucketConfiguration);

        final Injector defaultInjector = getDefaultInjector(serviceConfiguration);

        final RepairWorkerFactory repairWorkerFactory = defaultInjector.getInstance(RepairWorkerFactory.class);

        final QueueName queueName = QueueName.valueOf("repairer_republishes_newly_visible_in_tombstoned_bucket");

        final QueueDefinition queueDefinition = setupQueue(queueName, 1);

        repairWorkerFactory.forQueue(queueDefinition);

        final DataContextFactory contextFactory = defaultInjector.getInstance(DataContextFactory.class);

        final DataContext dataContext = contextFactory.forQueue(queueDefinition);

        final MonotonicIndex index = MonotonicIndex.valueOf(0);

        final Message message = Message.builder()
                                       .blob("BOO!")
                                       .index(index)
                                       .tag(MessageTag.random())
                                       .build();

        final RepairWorkerImpl repairWorker = (RepairWorkerImpl) repairWorkerFactory.forQueue(queueDefinition);

        repairWorker.start();

        dataContext.getMessageRepository().putMessage(message);

        getTestClock().tick();

        dataContext.getMessageRepository().tombstone(ReaderBucketPointer.valueOf(0));

        getTestClock().tickSeconds(5L);

        repairWorker.waitForNextRun();

        final Message repairedMessage = dataContext.getMessageRepository().getMessage(index);

        assertThat(repairedMessage.isAcked()).isTrue();

        final Message republish = dataContext.getMessageRepository().getMessage(MonotonicIndex.valueOf(1));

        assertThat(republish.getBlob()).isEqualTo(repairedMessage.getBlob());

        repairWorker.stop();
    }

    @Test
    public void repairer_moves_off_ghost_messages() throws InterruptedException, ExistingMonotonFoundException {

        final ServiceConfiguration serviceConfiguration = new ServiceConfiguration();

        final BucketConfiguration bucketConfiguration = new BucketConfiguration();

        bucketConfiguration.setRepairWorkerTimeout(Duration.standardSeconds(10));

        serviceConfiguration.setBucketConfiguration(bucketConfiguration);

        final Injector defaultInjector = getDefaultInjector(serviceConfiguration);

        final RepairWorkerFactory repairWorkerFactory = defaultInjector.getInstance(RepairWorkerFactory.class);

        final QueueName queueName = QueueName.valueOf("repairer_moves_off_ghost_messages");

        final QueueDefinition queueDefinition = setupQueue(queueName, 2);

        repairWorkerFactory.forQueue(queueDefinition);

        final DataContextFactory contextFactory = defaultInjector.getInstance(DataContextFactory.class);

        final DataContext dataContext = contextFactory.forQueue(queueDefinition);

        MonotonicIndex index = dataContext.getMonotonicRepository().nextMonotonic();

        Message message = Message.builder()
                                 .blob("BOO!")
                                 .index(index)
                                 .build();

        final RepairWorkerImpl repairWorker = (RepairWorkerImpl)repairWorkerFactory.forQueue(queueDefinition);

        RepairBucketPointer repairCurrentBucketPointer = dataContext.getPointerRepository().getRepairCurrentBucketPointer();

        assertThat(repairCurrentBucketPointer.get()).isEqualTo(0);

        dataContext.getMessageRepository().putMessage(message);

        getTestClock().tick();

        message = dataContext.getMessageRepository().getMessage(index);

        dataContext.getMessageRepository().ackMessage(message);

        repairWorker.start();

        // the ghost message
        dataContext.getMonotonicRepository().nextMonotonic();
        getTestClock().tick();


        // tombstone the old bucket
        dataContext.getMessageRepository().tombstone(ReaderBucketPointer.valueOf(0));

        index = dataContext.getMonotonicRepository().nextMonotonic();

        final Message thirdmessage = Message.builder().blob("3rd").index(index).build();

        dataContext.getMessageRepository().putMessage(thirdmessage);
        getTestClock().tickSeconds(50L);

        repairWorker.waitForNextRun();

        // assert that the repair pointer moved
        repairCurrentBucketPointer = dataContext.getPointerRepository().getRepairCurrentBucketPointer();

        assertThat(repairCurrentBucketPointer.get()).isEqualTo(1);

        repairWorker.stop();
    }
}
