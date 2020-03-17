package com.hydro17.spaceagencydatahub.models;

import lombok.Data;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
public class ProductOrderDTOOutput {

    private long id;
    private Instant placedOn;
    private List<ProductDTO> productDTOs = new ArrayList<>();

    public void addProductDTO(ProductDTO productDTO) {
        productDTOs.add(productDTO);
    }
}
