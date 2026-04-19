package com.parsun.parsun_api.auth.authentication.infrastructure.web;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.parsun.parsun_api.auth.authentication.application.exceptions.InvalidCredentialsException;
import com.parsun.parsun_api.auth.token.application.exceptions.InvalidTokenException;
import com.parsun.parsun_api.auth.token.application.exceptions.TokenExpiredException;
import com.parsun.parsun_api.auth.user.application.exceptions.DuplicateEmailException;
import com.parsun.parsun_api.auth.user.application.exceptions.UserNotFoundException;
import com.parsun.parsun_api.customers.application.exceptions.CustomerNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
@ResponseBody
public class AuthExceptionHandler {

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateEmail(DuplicateEmailException ex) {
        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.CONFLICT,
            "Email already registered",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorDetails);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(UserNotFoundException ex) {
        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.NOT_FOUND,
            "User not found",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDetails);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidCredentials(InvalidCredentialsException ex) {
        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.UNAUTHORIZED,
            "Invalid credentials",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorDetails);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidToken(InvalidTokenException ex) {
        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.UNAUTHORIZED,
            "Invalid token",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorDetails);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<Map<String, Object>> handleTokenExpired(TokenExpiredException ex) {
        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.UNAUTHORIZED,
            "Token expired",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorDetails);
    }

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCustomerNotFound(CustomerNotFoundException ex) {
        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.NOT_FOUND,
            "Customer not found",
            ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDetails);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Invalid request",
            ex.getMessage()
        );
        return ResponseEntity.badRequest().body(errorDetails);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));

        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Validation failed",
            errors
        );
        return ResponseEntity.badRequest().body(errorDetails);
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadable(
            org.springframework.http.converter.HttpMessageNotReadableException ex) {
        String message = "Invalid request body";
        Throwable cause = ex.getMostSpecificCause();
        if (cause != null) {
            message = cause.getMessage();
        }
        
        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Invalid value",
            message
        );
        return ResponseEntity.badRequest().body(errorDetails);
    }

    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidFormat(InvalidFormatException ex) {
        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Invalid value",
            ex.getMessage()
        );
        return ResponseEntity.badRequest().body(errorDetails);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAuthorizationDenied(AuthorizationDeniedException ex) {
        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.FORBIDDEN,
            "Access denied",
            "You do not have permission to access this resource"
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorDetails);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        ex.printStackTrace();
        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal server error",
            "An unexpected error occurred"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDetails);
    }

    private Map<String, Object> createErrorResponse(HttpStatus status, String error, String message) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now().toString());
        errorDetails.put("status", status.value());
        errorDetails.put("error", error);
        errorDetails.put("message", message);
        return errorDetails;
    }
}
