package com.hydro17.spaceagencydatahub.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@JsonPropertyOrder({"id", "missionName", "acquisitionDate", "footprint", "price", "url"})
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull
    private LocalDateTime acquisitionDate;

    @NotNull
    @OneToOne(cascade = CascadeType.ALL)
    private ProductFootprint footprint;

    @NotNull
    private BigDecimal price;

    @NotNull
    @Column(length = 1024)
    private String url;

    @JsonIgnore
    @ManyToOne()
    @NotNull
    @JoinColumn(name="mission_id")
    private Mission mission;

    public String getMissionName() {
        return mission.getName();
    }
}
