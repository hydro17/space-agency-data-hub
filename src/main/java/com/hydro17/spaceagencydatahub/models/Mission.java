package com.hydro17.spaceagencydatahub.models;

import com.hydro17.spaceagencydatahub.utils.ImageryType;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Data
public class Mission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(unique = true)
    @NotNull
    private String name;

    @NotNull
    private ImageryType imageryType;

    @NotNull
    private LocalDateTime startDate;

    @NotNull
    private LocalDateTime finishDate;
}
