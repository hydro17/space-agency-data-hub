package com.hydro17.spaceagencydatahub.services;

import com.hydro17.spaceagencydatahub.models.Product;
import com.hydro17.spaceagencydatahub.models.ProductAndOrderCount;
import com.hydro17.spaceagencydatahub.repositories.OrderItemRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderItemService {

    private OrderItemRepository orderItemRepository;

    public OrderItemService(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }

//    public List<Object[]> getAllProductAndOrderCountGroupedByProductIdOrderedByOrderCountDesc() {
//        return orderItemRepository.findAllProductAndOrderCountGroupedByProductIdOrderedByOrderCountDesc();
//    }

    public List<ProductAndOrderCount> getAllProductAndOrderCountGroupedByProductIdOrderedByOrderCountDesc() {
        return orderItemRepository.findAllProductAndOrderCountGroupedByProductIdOrderedByOrderCountDesc();
    }

    public List<Product> convertAllProductAndOrderCountToProduct(List<ProductAndOrderCount> productAndOrderCounts) {
        List<Product> products = new ArrayList<>();

        productAndOrderCounts.forEach(productAndOrderCount -> products.add(productAndOrderCount.getProduct()));

        return products;
    }
}
