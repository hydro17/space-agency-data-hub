package com.hydro17.spaceagencydatahub.services;

import com.hydro17.spaceagencydatahub.models.Mission;
import com.hydro17.spaceagencydatahub.models.Product;
import com.hydro17.spaceagencydatahub.models.ProductDTO;
import com.hydro17.spaceagencydatahub.repositories.MissionRepository;
import com.hydro17.spaceagencydatahub.repositories.ProductRepository;
import com.hydro17.spaceagencydatahub.repositories.ProductSpecifications;
import com.hydro17.spaceagencydatahub.utils.ImageryType;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private ProductRepository productRepository;
    private MissionService missionService;
    private ProductOrderService productOrderService;

    public ProductService(ProductRepository productRepository, MissionService missionService, ProductOrderService productOrderService) {
        this.productRepository = productRepository;
        this.missionService = missionService;
        this.productOrderService = productOrderService;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(long id) {
        Optional<Product> product = productRepository.findById(id);
        return product.orElse(null);
    }

    public List<Product> getFilteredProducts(String missionNme, LocalDateTime beforeDate, LocalDateTime afterDate,
                                             Double latitude, Double longitude, ImageryType imageryType) {
        return productRepository.findAll(ProductSpecifications.getSpecifications(missionNme, beforeDate, afterDate, latitude, longitude, imageryType));
    }

    public Product saveProduct(Product product) {
        Product productWithSetId = productRepository.save(product);
        return productWithSetId;
    }

    public void deleteProductById(long id) {
        productRepository.deleteById(id);
    }

    public boolean doesMissionExist(String missionName) {
        if (missionService.getMissionByName(missionName).isPresent()) return true;
        return false;
    }

    public Product convertProductDTOToProduct(ProductDTO productDTO) {
        Product product = new Product();

        product.setId(productDTO.getId());
        product.setAcquisitionDate(productDTO.getAcquisitionDate());
        product.setFootprint(productDTO.getFootprint());
        product.setPrice(productDTO.getPrice());
        product.setUrl(productDTO.getUrl());

        Mission mission = missionService.getMissionByName(productDTO.getMissionName()).get();
        mission.getProducts().add(product);
        product.setMission(mission);

        return product;
    }

    public List<Product> removeUrlOfUnorderedProducts(List<Product> products) {

            products.forEach(product -> {
                if (productOrderService.isOrderedProductWithGivenId(product.getId()) == false) {
                    product.setUrl(null);
                }
            });

        return products;
    }
}
