package com.ktt.exceptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ktt.dtos.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(Exception.class)
    public final ResponseEntity<Map<String, List<String>>> handleGeneralExceptions(Exception ex) {
        List<String> errors = List.of(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorsMap(errors));
    }

    @ExceptionHandler(RuntimeException.class)
    public final ResponseEntity<Map<String, List<String>>> handleRuntimeExceptions(RuntimeException ex) {
        List<String> errors = List.of(ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorsMap(errors));
    }

    @ExceptionHandler(InvalidJwtException.class)
    public ResponseEntity<Map<String, List<String>>> handleJwtErrors(InvalidJwtException ex) {

        List<String> errors = List.of(ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorsMap(errors));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, List<String>>> handleBadCredentialsError(BadCredentialsException ex) {

        List<String> errors = List.of(ex.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorsMap(errors));
    }

    private Map<String, List<String>> errorsMap(List<String> errors) {
        Map<String, List<String>> errorResponse = new HashMap<>();
        errorResponse.put("errors", errors);
        return errorResponse;
    }


//  // for profile exceptions
//  @ExceptionHandler(ResourceNotFoundException.class)
//  public ResponseEntity<ApiResponse> handleResourceNotFoundException(ResourceNotFoundException ex) {
//    return ResponseEntity.status(404).body(new ApiResponse(null, ex.getMessage(), null));
//  }
//
//  @ExceptionHandler(Exception.class)
//  public ResponseEntity<ApiResponse> handleGenericException(Exception ex) {
//    return ResponseEntity.status(500).body(new ApiResponse(null, "Internal Server Error", null));
//  }


}