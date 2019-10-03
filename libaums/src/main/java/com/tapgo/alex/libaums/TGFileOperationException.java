package com.tapgo.alex.libaums;

/**
 * Created by AlexHe on 2019-10-01.
 * Describe
 */

public class TGFileOperationException extends Exception {
    public TGFileOperationException() {
        super();
    }
    public TGFileOperationException(String message) {
        super(message);
    }
    public TGFileOperationException(String message, Throwable cause) {
        super(message, cause);
    }
    public TGFileOperationException(Throwable cause) {
        super(cause);
    }
}