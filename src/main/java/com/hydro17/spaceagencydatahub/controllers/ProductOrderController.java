package com.hydro17.spaceagencydatahub.controllers;

import com.hydro17.spaceagencydatahub.exceptions.ProductOrderNoOrderItemsException;
import com.hydro17.spaceagencydatahub.models.*;
import com.hydro17.spaceagencydatahub.services.ProductOrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class ProductOrderController {

    private ProductOrderService productOrderService;

    public ProductOrderController(ProductOrderService productOrderService) {
        this.productOrderService = productOrderService;
    }

    @GetMapping("/history")
    public List<ProductOrder> getAllProductOrdersOrderedByPlacedOnDesc() {
        List<ProductOrder> productOrders = productOrderService.getAllProductOrdersOrderedByPlacedOnDesc();
        return productOrders;
    }

    @PostMapping
    public ProductOrder addOrder(@RequestBody ProductOrderDTO productOrderDTO) {

        if (productOrderDTO == null || productOrderDTO.getProductIds().size() == 0) {
            throw new ProductOrderNoOrderItemsException("Order is empty. Order has to contain at least one product.");
        }

        ProductOrder productOrder = productOrderService.convertProductOrderDTOToProductOrder(productOrderDTO);

        ProductOrder productOrderWithSetId = productOrderService.saveProductOrder(productOrder);
        return productOrderWithSetId;
    }
}
