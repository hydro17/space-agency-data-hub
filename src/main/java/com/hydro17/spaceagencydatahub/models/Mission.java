package com.hydro17.spaceagencydatahub.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hydro17.spaceagencydatahub.utils.ImageryType;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @JsonIgnore
    @OneToMany(mappedBy = "mission",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH})
    private List<Product> products = new ArrayList<>();

    public void addProduct(Product product) {
        products.add(product);
    }
}
