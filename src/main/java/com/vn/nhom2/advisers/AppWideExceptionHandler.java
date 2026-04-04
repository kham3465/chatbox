package com.vn.nhom2.advisers;

import com.vn.nhom2.exception.ServerErrorException;
import com.vn.nhom2.exception.ClientErrorException;
import com.vn.nhom2.exception.ResourceConflictException;
import com.vn.nhom2.exception.ResourceNotFoundException;
import com.vn.nhom2.util.StandardResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
@CrossOrigin
public class AppWideExceptionHandler {
    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<StandardResponse> handleResourceConflictException(ResourceConflictException ex) {
        StandardResponse response = new StandardResponse(String.valueOf(HttpStatus.CONFLICT.value()), ex.getMessage(), null);
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception e) {
        return new ResponseEntity<>(new StandardResponse("500", "Error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ServerErrorException.class)
    public ResponseEntity<Object> handleServerException(ServerErrorException e) {
        return new ResponseEntity<>(new StandardResponse("500", "Error", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ClientErrorException.class)
    public ResponseEntity<Object> handleClientErrorException(ClientErrorException e) {
        return new ResponseEntity<>(new StandardResponse("400", "Error", e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException e) {
        return new ResponseEntity<>(new StandardResponse("404", "Not Found", e.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException e) {
        return new ResponseEntity<>(new StandardResponse("400", "Invalid Input", e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> validationError(MethodArgumentNotValidException ex) {
        List<String> errorMessages = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach(
                fieldError -> errorMessages.add(fieldError.getDefaultMessage())
        );
        return new ResponseEntity<>(new StandardResponse("400", "Error", String.join("\n", errorMessages)), HttpStatus.BAD_REQUEST);
    }
}
