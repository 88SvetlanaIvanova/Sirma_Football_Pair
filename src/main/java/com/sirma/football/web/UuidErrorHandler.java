package com.sirma.football.web;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

@ControllerAdvice
public class UuidErrorHandler {
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        if (ex.getRequiredType() == java.util.UUID.class) {
            return ResponseEntity.badRequest().body("Invalid UUID in path: " + ex.getValue());
        }
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
