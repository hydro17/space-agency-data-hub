package com.hydro17.spaceagencydatahub.services;

import com.hydro17.spaceagencydatahub.exceptions.ProductNotFoundException;
import com.hydro17.spaceagencydatahub.models.Product;
import com.hydro17.spaceagencydatahub.models.ProductOrder;
import com.hydro17.spaceagencydatahub.models.ProductOrderDTO;
import com.hydro17.spaceagencydatahub.repositories.ProductOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductOrderService {

    private ProductOrderRepository productOrderRepository;
    private ProductService productService;

    public ProductOrderService(ProductOrderRepository productOrderRepository) {
        this.productOrderRepository = productOrderRepository;
    }

    //  To avoid circular dependency
    @Autowired
    public void setProductService(ProductService productService) {
        this.productService = productService;
    }

    public List<ProductOrder> getAllProductOrdersOrderedByPlacedOnDesc() {
        return productOrderRepository.findAllOrdersByPlacedOnDesc();
    }

    public ProductOrder saveProductOrder(ProductOrder productOrder) {
        ProductOrder productOrderWithSetId = productOrderRepository.save(productOrder);
        return productOrderWithSetId;
    }

    public boolean isOrderedProductById(long id) {
        return productOrderRepository.findAllProductOrdersContainingProductWithGivenId(id).size() > 0;
    }

    public ProductOrder convertProductOrderDTOToProductOrder(ProductOrderDTO productOrderDTO) {
        ProductOrder productOrder = new ProductOrder();
        productOrder.setPlacedOn(LocalDateTime.now());

        productOrderDTO.getProductIds().forEach(productId -> {
            Product product = productService.getProductById(productId);

            if (product == null) {
                throw new ProductNotFoundException("There is no product with id: " + productId);
            }

            productOrder.addProduct(product);
        });
        return productOrder;
    }
}
