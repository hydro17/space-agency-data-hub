package com.hydro17.spaceagencydatahub.models;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ProductDTO {

    private long id;

    @NotNull
    private String missionName;

    @NotNull
    private LocalDateTime acquisitionDate;

    @NotNull
    private ProductFootprint footprint;

    @NotNull
    private BigDecimal price;

    @NotNull
    @Size(max=1024)
    private String url;
}
