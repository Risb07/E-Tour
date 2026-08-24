package com.etour.notification.web;

/** A referenced notification or template does not exist. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
