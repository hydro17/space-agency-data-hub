package com.hydro17.spaceagencydatahub.models;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ProductOrderDTO {

    private List<Long> productIds = new ArrayList<>();
}
