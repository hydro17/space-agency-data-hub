package com.hydro17.spaceagencydatahub.services;

import com.hydro17.spaceagencydatahub.models.Product;
import com.hydro17.spaceagencydatahub.repositories.ProductRepository;
import com.hydro17.spaceagencydatahub.repositories.ProductSpecifications;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(long id) {
        Optional<Product> product = productRepository.findById(id);
        return product.orElse(null);
    }

    public List<Product> getFilteredProducts(String missionNme, LocalDateTime beforeDate, LocalDateTime afterDate, Double latitude, Double longitude) {
        return productRepository.findAll(ProductSpecifications.getSpecifications(missionNme, beforeDate, afterDate, latitude, longitude));
    }

    public Product saveProduct(Product product) {
        Product productWithSetId = productRepository.save(product);
        return productWithSetId;
    }

    public void deleteProductById(long id) {
        productRepository.deleteById(id);
    }
}
