package com.hydro17.spaceagencydatahub.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Data
public class ProductOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull
    private LocalDateTime placedOn;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "productOrder")
    private List<OrderItem> orderItems = new ArrayList<>();

    public List<Long> getProductIds() {
        return orderItems.stream()
                .map(orderItem -> orderItem.getProduct().getId())
                .collect(Collectors.toList());
    }

    public void addProduct(Product product) {

        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setProductOrder(this);

        orderItems.add(orderItem);
    }
}
