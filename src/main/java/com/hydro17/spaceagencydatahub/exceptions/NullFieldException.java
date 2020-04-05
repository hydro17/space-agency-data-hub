package com.hydro17.spaceagencydatahub.exceptions;

import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.BindingResult;

@Getter
@Setter
public abstract class NullFieldException extends RuntimeException {

    private String message;
    private BindingResult bindingResult;

    public NullFieldException(String message, BindingResult bindingResult) {
        this.bindingResult = bindingResult;
        this.message = message;
    }
}
