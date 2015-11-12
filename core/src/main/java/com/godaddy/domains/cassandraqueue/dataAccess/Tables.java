package com.godaddy.domains.cassandraqueue.dataAccess;

public final class Tables {
    public static class Pointer {
        public static final String TABLE_NAME = "pointer";
        public static final String QUEUENAME = "queuename";
        public static final String POINTER_TYPE = "pointer_type";
        public static final String VALUE = "value";
    }

    public static class Message {

        public static final String TABLE_NAME = "message";
        public static final String QUEUENAME = "queuename";
        public static final String BUCKET_NUM = "bucket_num";
        public static final String MONOTON = "monoton";
        public static final String MESSAGE = "blob";
        public static final String VERSION = "blob";
        public static final String ACKED = "blob";
        public static final String NEXT_VISIBLE_ON = "next_visible_on";
        public static final String CREATED_DATE = "created_date";
    }
}
