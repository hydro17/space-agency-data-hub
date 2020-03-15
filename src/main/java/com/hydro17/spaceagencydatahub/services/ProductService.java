package com.hydro17.spaceagencydatahub.services;

import com.hydro17.spaceagencydatahub.models.Product;
import com.hydro17.spaceagencydatahub.repositories.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private ProductRepository _productRepository;

    public ProductService(ProductRepository productRepository) {
        this._productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return _productRepository.findAll();
    }

    public Product getProductById(long id) {
        Optional<Product> product = _productRepository.findById(id);
        return product.orElse(null);
    }

    public Product saveProduct(Product product) {
        Product productWithSetId = _productRepository.save(product);
        return productWithSetId;
    }

    public void deleteProductById(long id) {
        _productRepository.deleteById(id);
    }
}
