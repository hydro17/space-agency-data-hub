package com.hydro17.spaceagencydatahub.Converters;

import com.hydro17.spaceagencydatahub.exceptions.MissionNotFoundException;
import com.hydro17.spaceagencydatahub.models.Mission;
import com.hydro17.spaceagencydatahub.models.Product;
import com.hydro17.spaceagencydatahub.models.ProductDTO;
import com.hydro17.spaceagencydatahub.services.MissionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

public class ProductDtoToProductConverter implements Converter<ProductDTO, Product> {

    private final MissionService missionService;

    public ProductDtoToProductConverter(MissionService missionService) {
        this.missionService = missionService;
    }

    @Override
    public Product convert(ProductDTO productDTO) {
        Product product = new Product();

        product.setId(0);
        product.setAcquisitionDate(productDTO.getAcquisitionDate());
        product.setFootprint(productDTO.getFootprint());
        product.setPrice(productDTO.getPrice());
        product.setUrl(productDTO.getUrl());

        Mission mission = missionService.getMissionByName(productDTO.getMissionName()).orElseThrow(() -> {
            throw new MissionNotFoundException("There is no mission with the name: " + productDTO.getMissionName());
        });
        mission.addProduct(product);
        product.setMission(mission);

        return product;
    }
}
