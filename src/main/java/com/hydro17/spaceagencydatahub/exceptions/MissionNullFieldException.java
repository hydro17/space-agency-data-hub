package com.hydro17.spaceagencydatahub.exceptions;

import lombok.Getter;
import org.springframework.validation.BindingResult;

@Getter
public class MissionNullFieldException extends NullFieldException {

    public MissionNullFieldException(BindingResult bindingResult) {
        super("Mission fields: ", bindingResult);
    }
}
