package com.hydro17.spaceagencydatahub.models;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class ProductOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull
    private Instant placedOn;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "productOrder")
    private List<OrderItem> orderItems = new ArrayList<>();

    public void addProduct(Product product) {

        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setProductOrder(this);

        orderItems.add(orderItem);
    }
}
