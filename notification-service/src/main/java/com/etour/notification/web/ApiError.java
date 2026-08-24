package com.etour.notification.web;

import java.time.LocalDateTime;

/**
 * The error envelope both eTour backends already return
 * ({@code timestamp/status/message/path}).
 *
 * <p>Matching it matters: the React client's httpClient reads {@code data.message}
 * to build the toast it shows the user. A different shape here would surface as
 * "Request failed (400)" instead of the actual reason.
 */
public record ApiError(LocalDateTime timestamp, int status, String message, String path) {

    public static ApiError of(int status, String message, String path) {
        return new ApiError(LocalDateTime.now(), status, message, path);
    }
}
