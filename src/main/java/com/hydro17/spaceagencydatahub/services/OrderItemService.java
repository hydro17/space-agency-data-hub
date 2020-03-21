package com.hydro17.spaceagencydatahub.services;

import com.hydro17.spaceagencydatahub.models.IProductAndOrderCount;
import com.hydro17.spaceagencydatahub.models.Product;
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

    public List<IProductAndOrderCount> getAllProductAndOrderCountGroupedByProductIdOrderedByOrderCountDesc() {
        return orderItemRepository.findAllProductAndOrderCountGroupedByProductIdOrderedByOrderCountDesc();
    }

    public List<Product> convertAllProductAndOrderCountToProduct(List<IProductAndOrderCount> productAndOrderCounts) {
        List<Product> products = new ArrayList<>();

        productAndOrderCounts.forEach(productAndOrderCount -> products.add(productAndOrderCount.getProduct()));

        return products;
    }
}
