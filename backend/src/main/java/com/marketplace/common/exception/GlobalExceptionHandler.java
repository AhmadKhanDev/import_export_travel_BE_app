package com.marketplace.common.exception;

import com.marketplace.common.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), "NOT_FOUND", null);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), "BAD_REQUEST", null);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage(), "CONFLICT", null);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException ex) {
        return buildError(HttpStatus.FORBIDDEN, ex.getMessage(), "FORBIDDEN", null);
    }

    @ExceptionHandler(OwnershipException.class)
    public ResponseEntity<ErrorResponse> handleOwnership(OwnershipException ex) {
        return buildError(HttpStatus.FORBIDDEN, ex.getMessage(), "OWNERSHIP_VIOLATION", null);
    }

    @ExceptionHandler(InvalidStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStatus(InvalidStatusException ex) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), "INVALID_STATUS", null);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex) {
        return buildError(HttpStatus.UNAUTHORIZED, ex.getMessage(), "UNAUTHORIZED", null);
    }

    @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex) {
        return buildError(HttpStatus.UNAUTHORIZED, "Invalid email or password", "AUTH_FAILED", null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return buildError(HttpStatus.FORBIDDEN, "Access denied", "FORBIDDEN", null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldError)
                .collect(Collectors.toList());

        ErrorResponse body = ErrorResponse.builder()
                .success(false)
                .message("Validation failed")
                .errorCode("VALIDATION_ERROR")
                .fieldErrors(fieldErrors)
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", "INTERNAL_ERROR", null);
    }

    private ErrorResponse.FieldError toFieldError(FieldError fieldError) {
        return ErrorResponse.FieldError.builder()
                .field(fieldError.getField())
                .message(fieldError.getDefaultMessage())
                .rejectedValue(fieldError.getRejectedValue())
                .build();
    }

    private ResponseEntity<ErrorResponse> buildError(
            HttpStatus status, String message, String errorCode, List<ErrorResponse.FieldError> fieldErrors) {
        ErrorResponse body = ErrorResponse.builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .fieldErrors(fieldErrors)
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(status).body(body);
    }
}