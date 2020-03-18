package com.hydro17.spaceagencydatahub.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({MissionNotFoundException.class, ProductNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFoundException(RuntimeException ex) {

        ErrorResponse error = new ErrorResponse();
        error.setStatus(HttpStatus.NOT_FOUND.value());
        error.setMessage(ex.getMessage());

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({MissionNameNotUniqueException.class, MissionProductExistsException.class,
            MissionNullFieldException.class, ProductNullFieldException.class, ProductOrderNoOrderItemsException.class})
    public ResponseEntity<ErrorResponse> handleBadRequestException(RuntimeException ex) {

        ErrorResponse error = new ErrorResponse();
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setMessage(ex.getMessage());

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

//    @ExceptionHandler
//    public void handleExceptions(HttpMessageNotReadableException ex) {
//        throw new MissionNullFieldException("Bad data");
//    }

//    @ExceptionHandler
//    public ResponseEntity<String> handleAllExceptions(Exception ex, WebRequest request) {
//        return new ResponseEntity<>("|" + ex.toString() + "|", HttpStatus.BAD_REQUEST);
//    }
}
