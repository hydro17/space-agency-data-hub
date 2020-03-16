package com.hydro17.spaceagencydatahub.models;

import com.hydro17.spaceagencydatahub.utils.ImageryType;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class MissionDTO {

    private long id;

    @NotNull
    private String name;

    @NotNull
    private ImageryType imageryType;

    @NotNull
    private LocalDateTime startDate;

    @NotNull
    private LocalDateTime finishDate;
}
