package com.hydro17.spaceagencydatahub.exceptions;

import lombok.Data;
import org.springframework.validation.BindingResult;

@Data
public abstract class NullFieldException extends RuntimeException {

    private String message;
    private BindingResult bindingResult;

    public NullFieldException(String message, BindingResult bindingResult) {
        this.bindingResult = bindingResult;
        this.message = message;
    }
}
