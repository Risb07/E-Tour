package com.etour.exception;


import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;


import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;



@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);



    // Resource Not Found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse> handleNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {


        ApiResponse response =
                new ApiResponse(
                    LocalDateTime.now(),
                    404,
                    ex.getMessage(),
                    request.getRequestURI()
                );


        return new ResponseEntity<>(
                response,
                HttpStatus.NOT_FOUND
        );

    }

    // JPA Entity Not Found (service-layer 404s)
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse> handleEntityNotFound(
            EntityNotFoundException ex,
            HttpServletRequest request) {

        ApiResponse response =
                new ApiResponse(
                    LocalDateTime.now(),
                    404,
                    ex.getMessage(),
                    request.getRequestURI()
                );

        return new ResponseEntity<>(
                response,
                HttpStatus.NOT_FOUND
        );

    }

    // Unmapped route / static resource miss -> 404, not 500
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse> handleNoResource(
            NoResourceFoundException ex,
            HttpServletRequest request) {

        ApiResponse response =
                new ApiResponse(
                    LocalDateTime.now(),
                    404,
                    "No endpoint found for " + request.getMethod() + " " + request.getRequestURI(),
                    request.getRequestURI()
                );

        return new ResponseEntity<>(
                response,
                HttpStatus.NOT_FOUND
        );

    }

    // Bad credentials (wrong password / unknown email) -> 401
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request) {

        ApiResponse response =
                new ApiResponse(
                    LocalDateTime.now(),
                    401,
                    "Invalid email or password",
                    request.getRequestURI()
                );

        return new ResponseEntity<>(
                response,
                HttpStatus.UNAUTHORIZED
        );

    }

    // Forbidden: authenticated but not permitted (covers AuthorizationDeniedException)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {

        ApiResponse response =
                new ApiResponse(
                    LocalDateTime.now(),
                    403,
                    "You do not have permission to perform this action",
                    request.getRequestURI()
                );

        return new ResponseEntity<>(
                response,
                HttpStatus.FORBIDDEN
        );

    }

    // Referential-integrity violations (FK conflicts on delete, etc.) -> 409
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse> handleDataIntegrity(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {

        log.warn("Data integrity violation on {}: {}", request.getRequestURI(), ex.getMostSpecificCause().getMessage());

        ApiResponse response =
                new ApiResponse(
                    LocalDateTime.now(),
                    409,
                    "The operation conflicts with existing data",
                    request.getRequestURI()
                );

        return new ResponseEntity<>(
                response,
                HttpStatus.CONFLICT
        );

    }





    // Validation Errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,String>> handleValidation(
            MethodArgumentNotValidException ex) {


        Map<String,String> errors = new HashMap<>();


        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error -> {

                    errors.put(
                        error.getField(),
                        error.getDefaultMessage()
                    );

                });


        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errors);

    }





    // Conflict (e.g. duplicate email)
    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<ApiResponse> handleConflict(
            ResourceConflictException ex,
            HttpServletRequest request) {

        ApiResponse response =
                new ApiResponse(
                    LocalDateTime.now(),
                    409,
                    ex.getMessage(),
                    request.getRequestURI()
                );

        return new ResponseEntity<>(
                response,
                HttpStatus.CONFLICT
        );
    }



    // Business rule violations (seats unavailable, unauthorized action, etc.)
    @ExceptionHandler(IllegalOperationException.class)
    public ResponseEntity<ApiResponse> handleIllegalOperation(
            IllegalOperationException ex,
            HttpServletRequest request) {

        ApiResponse response =
                new ApiResponse(
                    LocalDateTime.now(),
                    409,
                    ex.getMessage(),
                    request.getRequestURI()
                );

        return new ResponseEntity<>(
                response,
                HttpStatus.CONFLICT
        );
    }



    // Illegal Arguments
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> handleBadRequest(
            IllegalArgumentException ex,
            HttpServletRequest request) {


        ApiResponse response =
                new ApiResponse(
                    LocalDateTime.now(),
                    400,
                    ex.getMessage(),
                    request.getRequestURI()
                );


        return new ResponseEntity<>(
                response,
                HttpStatus.BAD_REQUEST
        );

    }

    // Persist/query-time bean validation (e.g. a missing required field on an
    // entity saved from a service, not caught at the @Valid request boundary).
    // Distinct from MethodArgumentNotValidException - that one handles @Valid
    // on the request body; this one handles validation triggered on save/flush.
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolation(
            ConstraintViolationException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(v ->
                errors.put(v.getPropertyPath().toString(), v.getMessage()));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errors);
    }

    // Malformed JSON body (unparseable content) -> 400, not 500
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse> handleUnreadableBody(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        ApiResponse response =
                new ApiResponse(
                    LocalDateTime.now(),
                    400,
                    "Malformed request body - check the JSON you are sending",
                    request.getRequestURI()
                );

        return new ResponseEntity<>(
                response,
                HttpStatus.BAD_REQUEST
        );

    }

    // Type mismatch on a path/query param (e.g. "abc" for a numeric id or a
    // bad date/enum value) -> 400, not 500
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        ApiResponse response =
                new ApiResponse(
                    LocalDateTime.now(),
                    400,
                    "Invalid value for parameter '" + ex.getName() + "'",
                    request.getRequestURI()
                );

        return new ResponseEntity<>(
                response,
                HttpStatus.BAD_REQUEST
        );

    }

    // Right URL, wrong verb (e.g. GET on a POST-only endpoint) -> 405, not 500.
    // Without this the catch-all below turned every method mismatch into an
    // "unexpected error" 500 and logged it at ERROR as if the server had
    // broken, when the request was simply addressed wrongly.
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request) {

        String supported = ex.getSupportedHttpMethods() == null
                ? ""
                : " Supported: " + String.join(", ",
                        ex.getSupportedHttpMethods().stream().map(Object::toString).toList()) + ".";

        ApiResponse response =
                new ApiResponse(
                    LocalDateTime.now(),
                    405,
                    request.getMethod() + " is not supported for " + request.getRequestURI() + "." + supported,
                    request.getRequestURI()
                );

        return new ResponseEntity<>(
                response,
                HttpStatus.METHOD_NOT_ALLOWED
        );

    }

    // Any Other Exception
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleException(
            Exception ex,
            HttpServletRequest request) {

        log.error("Unhandled exception on {}", request.getRequestURI(), ex);

        ApiResponse response =
                new ApiResponse(
                    LocalDateTime.now(),
                    500,
                    "An unexpected error occurred. Please try again later.",
                    request.getRequestURI()
                );

        return new ResponseEntity<>(
                response,
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

}
