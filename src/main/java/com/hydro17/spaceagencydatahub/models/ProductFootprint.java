package com.hydro17.spaceagencydatahub.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Getter
@Setter
@EqualsAndHashCode
public class ProductFootprint {

    @JsonIgnore
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
