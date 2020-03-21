package com.hydro17.spaceagencydatahub.models;

import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
public class ProductOrderDTOOutput {

    private long id;
    private Instant placedOn;
    private List<Product> products = new ArrayList<>();

    public void addProduct(Product product) {
        products.add(product);
    }
}
