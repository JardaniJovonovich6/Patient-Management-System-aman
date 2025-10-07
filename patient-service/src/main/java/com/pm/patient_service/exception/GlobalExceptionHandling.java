package com.pm.patient_service.exception;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandling {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandling.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException ex) {

        // Create a map to hold the error messages (field name -> error message)
        Map<String, String> errors = new HashMap<>();

        // Get all the field errors from the exception and put them into the map
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });

        // Return an HTTP 400 Bad Request response with the map of errors as the JSON
        // body
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex) {

        logger.warn("Email Already exist in the System : {}" + ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("errorMessage", ex.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<Map<String, String>> handlePatientNotFoundException(PatientNotFoundException ex) {
        logger.warn("Patient Not Found In the System with the ID : {}" + ex.getMessage());
        Map<String, String> error = new HashMap<>();
        error.put("PatientNotFoundExceptionError", ex.getMessage());
        return ResponseEntity.badRequest().body(error);

    }
}
