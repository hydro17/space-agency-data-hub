package com.hydro17.spaceagencydatahub.models;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Data
public class ProductFootprint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull
    private double startCoordinateLatitude;

    @NotNull
    private double startCoordinateLongitude;

    @NotNull
    private double endCoordinateLatitude;

    @NotNull
    private double endCoordinateLongitude;
}
