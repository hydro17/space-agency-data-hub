package com.hydro17.spaceagencydatahub.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({MissionNotFoundException.class, ProductNotFoundException.class})
    public ErrorResponse handleNotFoundException(RuntimeException ex) {

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(HttpStatus.NOT_FOUND.value());
        errorResponse.setMessage(ex.getMessage());

        return errorResponse;
    }

    @ExceptionHandler({MissionNullFieldException.class, ProductNullFieldException.class})
    public ResponseEntity<ErrorResponse> handleNullFieldException(NullFieldException ex) {
        String message = ex.getMessage();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            message += "'" + fieldError.getField() + "' " + fieldError.getDefaultMessage() + ", ";
        }

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setMessage(message.substring(0, message.length() - 2));

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({MissionNameNotUniqueException.class, MissionProductExistsException.class,
            ProductOrderNoOrderItemsException.class, ProductIsOrderedException.class,
            ProductBadAcquisitionDateException.class, ProductBadFindProductParameterException.class})
    public ResponseEntity<ErrorResponse> handleBadRequestException(RuntimeException ex) {

        ErrorResponse error = new ErrorResponse();
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setMessage(ex.getMessage());

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleExceptions(HttpMessageNotReadableException ex) {

        ErrorResponse error = new ErrorResponse();
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setMessage("Incorrect input data");

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

//    @ExceptionHandler
//    public ResponseEntity<String> handleAllExceptions(Exception ex, WebRequest request) {
//        return new ResponseEntity<>("|" + ex.toString() + "|", HttpStatus.BAD_REQUEST);
//    }
}
