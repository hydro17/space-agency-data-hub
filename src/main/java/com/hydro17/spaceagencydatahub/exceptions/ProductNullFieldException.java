package com.hydro17.spaceagencydatahub.exceptions;

import org.springframework.validation.BindingResult;

public class ProductNullFieldException extends NullFieldException {

    public ProductNullFieldException(BindingResult bindingResult) {
        super("Product fields: ", bindingResult);
    }
}
