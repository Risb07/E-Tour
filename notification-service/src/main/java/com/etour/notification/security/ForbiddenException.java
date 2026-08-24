package com.etour.notification.security;

/** Caller is authenticated but is not an eTour administrator. */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
