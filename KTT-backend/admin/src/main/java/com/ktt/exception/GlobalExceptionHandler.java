package com.ktt.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TenantAlreadyExistsException.class)
    public ResponseEntity<Map<String, List<String>>> handleTenantExists(TenantAlreadyExistsException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(TenantNotFoundException.class)
    public ResponseEntity<Map<String, List<String>>> handleTenantNotFound(TenantNotFoundException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, List<String>>> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .toList();
        return buildErrorResponse(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, List<String>>> handleGeneral(Exception ex) {
        return buildErrorResponse("An unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<Map<String, List<String>>> buildErrorResponse(String message, HttpStatus status) {
        return buildErrorResponse(List.of(message), status);
    }

    private ResponseEntity<Map<String, List<String>>> buildErrorResponse(List<String> messages, HttpStatus status) {
        Map<String, List<String>> errorMap = new HashMap<>();
        errorMap.put("errors", messages);
        return new ResponseEntity<>(errorMap, status);
    }
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, List<String>>> handleNoResourceFound(NoResourceFoundException ex) {
        return buildErrorResponse("Requested resource not found", HttpStatus.NOT_FOUND);
    }
}
