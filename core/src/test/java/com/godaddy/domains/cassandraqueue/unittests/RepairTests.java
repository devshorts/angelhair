package com.godaddy.domains.cassandraqueue.unittests;

import com.godaddy.domains.cassandraqueue.ServiceConfiguration;
import com.godaddy.domains.cassandraqueue.dataAccess.exceptions.ExistingMonotonFoundException;
import com.godaddy.domains.cassandraqueue.factories.DataContext;
import com.godaddy.domains.cassandraqueue.factories.DataContextFactory;
import com.godaddy.domains.cassandraqueue.factories.RepairWorkerFactory;
import com.godaddy.domains.cassandraqueue.model.Message;
import com.godaddy.domains.cassandraqueue.model.MonotonicIndex;
import com.godaddy.domains.cassandraqueue.model.QueueName;
import com.godaddy.domains.cassandraqueue.model.ReaderBucketPointer;
import com.godaddy.domains.cassandraqueue.workers.BucketConfiguration;
import com.godaddy.domains.cassandraqueue.workers.RepairWorker;
import com.google.inject.Injector;
import org.joda.time.Duration;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RepairTests extends TestBase {
    @Test
    public void repairer_republishes_newly_visible_in_tombstoned_bucket() throws InterruptedException, ExistingMonotonFoundException {

        final ServiceConfiguration serviceConfiguration = new ServiceConfiguration();

        final BucketConfiguration bucketConfiguration = new BucketConfiguration();

        bucketConfiguration.setBucketSize(1);
        bucketConfiguration.setRepairWorkerTimeout(Duration.standardSeconds(3));

        serviceConfiguration.setBucketConfiguration(bucketConfiguration);

        final Injector defaultInjector = getDefaultInjector(serviceConfiguration);

        final RepairWorkerFactory repairWorkerFactory = defaultInjector.getInstance(RepairWorkerFactory.class);

        final QueueName queueName = QueueName.valueOf("test_repairs");

        repairWorkerFactory.forQueue(queueName);

        final DataContextFactory contextFactory = defaultInjector.getInstance(DataContextFactory.class);

        final DataContext dataContext = contextFactory.forQueue(queueName);

        final MonotonicIndex index = MonotonicIndex.valueOf(0);

        final Message message = Message.builder()
                                       .blob("BOO!")
                                       .index(index)
                                       .build();

        final RepairWorker repairWorker = repairWorkerFactory.forQueue(queueName);

        repairWorker.start();

        dataContext.getMessageRepository().putMessage(message);

        dataContext.getMessageRepository().tombstone(ReaderBucketPointer.valueOf(0));

        Thread.sleep(5000);

        final Message repairedMessage = dataContext.getMessageRepository().getMessage(index);

        assertThat(repairedMessage.isAcked()).isTrue();

        final Message republish = dataContext.getMessageRepository().getMessage(MonotonicIndex.valueOf(1));

        assertThat(republish.getBlob()).isEqualTo(repairedMessage.getBlob());
    }
}
