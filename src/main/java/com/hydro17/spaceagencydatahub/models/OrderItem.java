package com.hydro17.spaceagencydatahub.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Getter
@Setter
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne
    private Product product;

    @ManyToOne
    @JoinColumn(name="product_order_id")
    private ProductOrder productOrder;
}
