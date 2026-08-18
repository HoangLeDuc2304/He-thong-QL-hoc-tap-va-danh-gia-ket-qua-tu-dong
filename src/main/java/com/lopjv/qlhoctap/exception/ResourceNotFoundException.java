package com.lopjv.qlhoctap.exception;

/**
 * Ngoại lệ ném ra khi không tìm thấy tài nguyên (Entity, User, Exam, Subject, etc.) trong Database.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
