package com.hydro17.spaceagencydatahub.exceptions;

public class MissionNotFoundException extends RuntimeException {

    public MissionNotFoundException(String message) {
        super(message);
    }
}
