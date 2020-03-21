package com.hydro17.spaceagencydatahub.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductAndOrderCount {
    private Product product;
    private Long orderCount;
}
