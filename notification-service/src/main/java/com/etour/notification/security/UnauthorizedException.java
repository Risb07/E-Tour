package com.etour.notification.security;

/** Caller presented no token, or one this service could not verify. */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
