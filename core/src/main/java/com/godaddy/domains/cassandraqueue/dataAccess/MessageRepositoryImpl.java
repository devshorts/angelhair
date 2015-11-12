package com.godaddy.domains.cassandraqueue.dataAccess;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.godaddy.domains.cassandraqueue.dataAccess.interfaces.MessageRepository;
import com.godaddy.domains.cassandraqueue.model.BucketPointer;
import com.godaddy.domains.cassandraqueue.model.Message;
import com.godaddy.domains.cassandraqueue.model.MessagePointer;
import com.godaddy.domains.cassandraqueue.model.QueueName;
import com.godaddy.domains.cassandraqueue.model.ReaderBucketPointer;
import com.godaddy.domains.cassandraqueue.workers.BucketConfiguration;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;

import java.util.List;
import java.util.Optional;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.set;
import static java.util.stream.Collectors.toList;

public class MessageRepositoryImpl extends RepositoryBase implements MessageRepository {
    private final Session session;
    private final QueueName queueName;
    private final BucketConfiguration bucketConfiguration;

    @Inject
    public MessageRepositoryImpl(Session session, @Assisted QueueName queueName, BucketConfiguration bucketConfiguration) {
        this.session = session;
        this.queueName = queueName;
        this.bucketConfiguration = bucketConfiguration;
    }

    @Override
    public void putMessage(final Message message, final Duration initialInvisibility) {
        final DateTime now = DateTime.now(DateTimeZone.UTC);
        Statement statement = QueryBuilder.insertInto(Tables.Message.TABLE_NAME)
                                          .ifNotExists()
                                          .value(Tables.Message.QUEUENAME, queueName.get())
                                          .value(Tables.Message.BUCKET_NUM, message.getIndex().toBucketPointer(bucketConfiguration.getBucketSize()).get())
                                          .value(Tables.Message.MONOTON, message.getIndex().get())
                                          .value(Tables.Message.VERSION, 1)
                                          .value(Tables.Message.ACKED, false)
                                          .value(Tables.Message.MESSAGE, message.getBlob())
                                          .value(Tables.Message.NEXT_VISIBLE_ON, now.plus(initialInvisibility).toDate())
                                          .value(Tables.Message.CREATED_DATE, now.toDate());

        session.execute(statement);
    }

    @Override
    public boolean markMessageInvisible(final Message message, final Duration duration) {
        // update message invisiblity value to utc now + duration
        // conditionally update index to use invisiblity if version the same

        final DateTime now = DateTime.now(DateTimeZone.UTC).plus(duration);

        Statement statement = QueryBuilder.update(Tables.Message.TABLE_NAME)
                                          .with(set(Tables.Message.NEXT_VISIBLE_ON, now.toDate()))
                                          .and(set(Tables.Message.VERSION, message.getVersion() + 1))
                                          .where(eq(Tables.Message.QUEUENAME, queueName.get()))
                                          .and(eq(Tables.Message.BUCKET_NUM, message.getIndex().toBucketPointer(bucketConfiguration.getBucketSize())))
                                          .and(eq(Tables.Message.MONOTON, message.getIndex().get()))
                                          .onlyIf(eq(Tables.Message.VERSION, message.getVersion()));


        return session.execute(statement).wasApplied();
    }

    @Override
    public boolean ackMessage(final Message message) {
        // conditionally ack if message version is the same as in the message
        //  if was able to update then return true, otehrwise false

        Statement statement = QueryBuilder.update(Tables.Message.TABLE_NAME)
                                          .with(set(Tables.Message.ACKED, true))
                                          .and(set(Tables.Message.VERSION, message.getVersion() + 1))
                                          .where(eq(Tables.Message.QUEUENAME, queueName.get()))
                                          .and(eq(Tables.Message.BUCKET_NUM, message.getIndex().toBucketPointer(bucketConfiguration.getBucketSize())))
                                          .and(eq(Tables.Message.MONOTON, message.getIndex().get()))
                                          .onlyIf(eq(Tables.Message.VERSION, message.getVersion()));

        return session.execute(statement).wasApplied();
    }

    @Override
    public void tombstone(final ReaderBucketPointer bucketPointer) {
        // mark the bucket as tombstoned

        final DateTime now = DateTime.now(DateTimeZone.UTC);
        Statement statement = QueryBuilder.insertInto(Tables.Message.TABLE_NAME)
                                          .ifNotExists()
                                          .value(Tables.Message.QUEUENAME, queueName.get())
                                          .value(Tables.Message.BUCKET_NUM, bucketPointer.get())
                                          .value(Tables.Message.MONOTON, -1)
                                          .value(Tables.Message.CREATED_DATE, now.toDate());

        session.execute(statement);
    }

    private Select.Where getReadMessageQuery(final BucketPointer bucketPointer) {
        return QueryBuilder.select()
                           .all()
                           .from(Tables.Message.TABLE_NAME)
                           .where(eq(Tables.Message.QUEUENAME, queueName.get()))
                           .and(eq(Tables.Message.BUCKET_NUM, bucketPointer.get()));
    }

    @Override
    public List<Message> getMessages(final BucketPointer bucketPointer) {
        // list all messages in bucket
        Statement query = getReadMessageQuery(bucketPointer);

        return session.execute(query)
                      .all()
                      .stream()
                      .map(Message::fromRow)
                      .filter(m -> m.getIndex().get() >= 0)
                      .collect(toList());
    }

    @Override
    public Optional<DateTime> tombstoneExists(final BucketPointer bucketPointer) {
        Statement query = getReadMessageQuery(bucketPointer);

        return Optional.ofNullable(getOne(session.execute(query), row -> new DateTime(row.getDate(Tables.Message.CREATED_DATE))));
    }

    @Override
    public Message getMessage(final MessagePointer pointer) {
        final BucketPointer bucketPointer = pointer.toBucketPointer(bucketConfiguration.getBucketSize());
        Statement query = getReadMessageQuery(bucketPointer);

        return getOne(session.execute(query), Message::fromRow);
    }


}
