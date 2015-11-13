package com.godaddy.domains.cassandraqueue.dataAccess.exceptions;

public class ExistingMonotonFoundException extends Exception {
    public ExistingMonotonFoundException(final String message) {
        super(message);
    }
}
