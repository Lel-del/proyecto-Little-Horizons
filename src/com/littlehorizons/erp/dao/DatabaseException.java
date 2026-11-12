package com.littlehorizons.erp.dao;

/**
 * Unchecked exception wrapper for data access failures, preserving the SQL cause for logging.
 */
public class DatabaseException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatabaseException(String message) {
        super(message);
    }
}
