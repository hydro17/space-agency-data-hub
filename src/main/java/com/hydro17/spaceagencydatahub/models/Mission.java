package com.hydro17.spaceagencydatahub.models;

import com.hydro17.spaceagencydatahub.utils.ImageryType;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
public class Mission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(unique = true)
    private String name;

    private ImageryType imageryType;

    private LocalDateTime startDate;

    private LocalDateTime finishDate;
}
