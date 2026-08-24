package com.etour.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.HttpRequestMethodNotSupportedException;

/**
 * The handler translates exceptions into the status codes the frontend's
 * httpClient branches on, so the mapping is worth pinning directly. These are
 * plain calls into the advice - no Spring context needed.
 */
@DisplayName("GlobalExceptionHandler status mapping")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private MockHttpServletRequest request(String method, String uri) {
        MockHttpServletRequest req = new MockHttpServletRequest(method, uri);
        req.setRequestURI(uri);
        return req;
    }

    @Test
    @DisplayName("a wrong HTTP verb is 405, not a 500 'unexpected error'")
    void methodNotSupportedIs405() {
        // Regression test. /api/passengers and /api/payments are POST-only;
        // a GET on them fell through to the catch-all Exception handler and
        // came back as 500, logged at ERROR as if the server had broken.
        HttpRequestMethodNotSupportedException ex =
                new HttpRequestMethodNotSupportedException("GET", java.util.List.of("POST"));

        ResponseEntity<ApiResponse> response =
                handler.handleMethodNotSupported(ex, request("GET", "/api/payments"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(405);
        assertThat(response.getBody().getPath()).isEqualTo("/api/payments");
    }

    @Test
    @DisplayName("the 405 message names the verb, the path and what is allowed")
    void methodNotSupportedMessageIsActionable() {
        HttpRequestMethodNotSupportedException ex =
                new HttpRequestMethodNotSupportedException("GET", java.util.List.of("POST"));

        ResponseEntity<ApiResponse> response =
                handler.handleMethodNotSupported(ex, request("GET", "/api/passengers"));

        assertThat(response.getBody().getMessage())
                .contains("GET")
                .contains("/api/passengers")
                .contains("POST");
    }

    @Test
    @DisplayName("a 405 with no declared supported methods still responds cleanly")
    void methodNotSupportedWithoutSupportedList() {
        // getSupportedHttpMethods() is nullable - the handler must not NPE and
        // turn a 405 back into the 500 it was written to prevent.
        HttpRequestMethodNotSupportedException ex =
                new HttpRequestMethodNotSupportedException("TRACE");

        ResponseEntity<ApiResponse> response =
                handler.handleMethodNotSupported(ex, request("TRACE", "/api/tours"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        assertThat(response.getBody().getMessage()).contains("/api/tours");
    }

    @Test
    @DisplayName("a missing resource is 404")
    void resourceNotFoundIs404() {
        ResponseEntity<ApiResponse> response = handler.handleNotFound(
                new ResourceNotFoundException("Tour not found: 99"), request("GET", "/api/tours/99"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().getMessage()).isEqualTo("Tour not found: 99");
    }

    @Test
    @DisplayName("a business-rule violation is 409, so the UI can show the reason")
    void illegalOperationIs409() {
        ResponseEntity<ApiResponse> response = handler.handleIllegalOperation(
                new IllegalOperationException("Only 3 seats left on this date."),
                request("POST", "/api/bookings"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        // The real message must survive - the frontend shows it verbatim in a toast.
        assertThat(response.getBody().getMessage()).isEqualTo("Only 3 seats left on this date.");
    }

    @Test
    @DisplayName("the catch-all is 500 and does not leak the exception message")
    void unexpectedIs500AndOpaque() {
        ResponseEntity<ApiResponse> response = handler.handleException(
                new RuntimeException("jdbc url jdbc:mysql://prod-host/etour?password=hunter2"),
                request("GET", "/api/tours"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getMessage()).doesNotContain("hunter2");
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred. Please try again later.");
    }
}
