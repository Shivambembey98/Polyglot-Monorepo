//package com.ktt.exception;
//
//import org.apache.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
//
//@RestControllerAdvice
//public class GlobalExceptionHandler {
//
//    @ExceptionHandler(CustomException.class)
//    public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex) {
//        return ResponseEntity.status(HttpStatus.SC_BAD_REQUEST)
//                .body(new ErrorResponse(ex.getMessage(), ex.getErrorCode()));
//    }
//
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
//        return ResponseEntity.status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
//                .body(new ErrorResponse(ErrorMessages.INTERNAL_ERROR, "INTERNAL_SERVER_ERROR"));
//    }
//
//    public record ErrorResponse(String message, String errorCode) {}
//}