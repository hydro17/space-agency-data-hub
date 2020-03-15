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
    @Column(name="start_coordinate_x")
    private double startCoordinateX;

    @NotNull
    @Column(name="start_coordinate_y")
    private double startCoordinateY;

    @NotNull
    @Column(name="end_coordinate_x")
    private double endCoordinateX;

    @NotNull
    @Column(name="end_coordinate_y")
    private double endCoordinateY;
}
