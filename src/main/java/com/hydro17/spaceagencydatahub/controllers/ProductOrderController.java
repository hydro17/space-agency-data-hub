package com.hydro17.spaceagencydatahub.controllers;

import com.hydro17.spaceagencydatahub.exceptions.ProductNotFoundException;
import com.hydro17.spaceagencydatahub.exceptions.ProductOrderNoOrderItemsException;
import com.hydro17.spaceagencydatahub.models.*;
import com.hydro17.spaceagencydatahub.services.ProductOrderService;
import com.hydro17.spaceagencydatahub.services.ProductService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class ProductOrderController {

    private ProductOrderService productOrderService;
    private ProductService productService;

    public ProductOrderController(ProductOrderService productOrderService, ProductService productService) {
        this.productOrderService = productOrderService;
        this.productService = productService;
    }

    @GetMapping("/history")
    public List<ProductOrder> getAllProductOrdersOrderedByPlacedOnDesc() {
        List<ProductOrder> productOrders = productOrderService.getAllProductOrdersOrderedByPlacedOnDesc();
        return productOrders;
    }

    @PostMapping
    public ProductOrder addOrder(@RequestBody ProductOrderDTOInput productOrderDTOInput) {

        if (productOrderDTOInput == null || productOrderDTOInput.getProductIds().size() == 0) {
            throw new ProductOrderNoOrderItemsException("Order is empty. Order has to contain at least one product.");
        }

        ProductOrder productOrder = convertProductOrderDTOInputToProductOrder(productOrderDTOInput);

        ProductOrder productOrderWithSetId = productOrderService.saveProductOrder(productOrder);
        return productOrderWithSetId;
    }

    private ProductOrder convertProductOrderDTOInputToProductOrder(ProductOrderDTOInput productOrderDTOInput) {
        ProductOrder productOrder = new ProductOrder();
        productOrder.setPlacedOn(LocalDateTime.now());

        productOrderDTOInput.getProductIds().forEach(productId -> {
            Product product = productService.getProductById(productId);

            if (product == null) {
                throw new ProductNotFoundException("There is no product with id: " + productId);
            }

            productOrder.addProduct(product);
        });
        return productOrder;
    }
}
