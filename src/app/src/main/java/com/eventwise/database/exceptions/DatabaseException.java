package com.eventwise.database.exceptions;

public class DatabaseException extends RuntimeException{

    // Default constructor
    public DatabaseException() {
        super();
    }

    // Constructor that accepts a message
    public DatabaseException(String message) {
        super(message);
    }

    // Constructor that accepts a message and a cause (another exception)
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    // Constructor that accepts a cause
    public DatabaseException(Throwable cause) {
        super(cause);
    }

}

