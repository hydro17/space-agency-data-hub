package com.hydro17.spaceagencydatahub.exceptions;

import lombok.Data;

@Data
public class ErrorResponse {
    private int status;
    private String message;
}
