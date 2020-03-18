package com.hydro17.spaceagencydatahub.services;

import com.hydro17.spaceagencydatahub.models.ProductOrder;
import com.hydro17.spaceagencydatahub.repositories.ProductOrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductOrderService {

    private ProductOrderRepository productOrderRepository;

    public ProductOrderService(ProductOrderRepository productOrderRepository) {
        this.productOrderRepository = productOrderRepository;
    }

    public List<ProductOrder> getAllOrders() {
        return productOrderRepository.findAll();
    }

    public List<ProductOrder> getAllProductOrdersOrderedByPlacedOn() {
       return productOrderRepository.findAllOrderByPlacedOn();
    }

    public List<ProductOrder> getAllProductOrdersOrderedByPlacedOnDesc() {
        return productOrderRepository.findAllOrderByPlacedOnDesc();
    }

    public ProductOrder saveProductOrder(ProductOrder productOrder) {
        ProductOrder productOrderWithSetId = productOrderRepository.save(productOrder);
        return productOrderWithSetId;
    }

    public boolean isOrderedProductWithGivenId(long id) {
        return productOrderRepository.findAllProductOrdersContainingProductWithGivenId(id).size() > 0;
    }
}
