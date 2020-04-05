package com.hydro17.spaceagencydatahub.services;

import com.hydro17.spaceagencydatahub.models.Product;
import com.hydro17.spaceagencydatahub.repositories.ProductRepository;
import com.hydro17.spaceagencydatahub.repositories.ProductSpecifications;
import com.hydro17.spaceagencydatahub.utils.ImageryType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private ProductRepository productRepository;
    private MissionService missionService;
    private ProductOrderService productOrderService;

    public ProductService(ProductRepository productRepository, MissionService missionService) {
        this.productRepository = productRepository;
        this.missionService = missionService;
    }

    //  To avoid circular dependency
    @Autowired
    public void setProductOrderService(ProductOrderService productOrderService) {
        this.productOrderService = productOrderService;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(long id) {
        return productRepository.findById(id);
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

    public List<Product> removeUrlOfUnorderedProducts(List<Product> products) {

            products.forEach(product -> {
                if (productOrderService.isOrderedProductById(product.getId()) == false) {
                    product.setUrl(null);
                }
            });

        return products;
    }
}
