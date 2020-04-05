package com.hydro17.spaceagencydatahub.Converters;

import com.hydro17.spaceagencydatahub.models.Product;
import com.hydro17.spaceagencydatahub.models.ProductDTO;
import org.springframework.core.convert.converter.Converter;

public class ProductToProductDTOConverter implements Converter<Product, ProductDTO> {
    @Override
    public ProductDTO convert(Product product) {
        ProductDTO productDTO = new ProductDTO();

        productDTO.setId(product.getId());
        productDTO.setMissionName(product.getMission().getName());
        productDTO.setAcquisitionDate(product.getAcquisitionDate());
        productDTO.setFootprint(product.getFootprint());
        productDTO.setPrice(product.getPrice());
        productDTO.setUrl(product.getUrl());

        return productDTO;
    }
}
