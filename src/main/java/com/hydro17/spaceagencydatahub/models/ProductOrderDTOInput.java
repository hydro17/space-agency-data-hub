package com.hydro17.spaceagencydatahub.models;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProductOrderDTOInput {

    private List<Long> productIds = new ArrayList<>();
}
