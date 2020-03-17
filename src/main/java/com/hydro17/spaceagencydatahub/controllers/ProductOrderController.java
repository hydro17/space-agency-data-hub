package com.hydro17.spaceagencydatahub.controllers;

import com.hydro17.spaceagencydatahub.exceptions.ErrorResponse;
import com.hydro17.spaceagencydatahub.exceptions.ProductErrorResponse;
import com.hydro17.spaceagencydatahub.exceptions.ProductNotFoundException;
import com.hydro17.spaceagencydatahub.exceptions.ProductOrderNoOrderItemsException;
import com.hydro17.spaceagencydatahub.models.*;
import com.hydro17.spaceagencydatahub.services.ProductOrderService;
import com.hydro17.spaceagencydatahub.services.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
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

    @GetMapping
    public List<ProductOrderDTOOutput> getAllProductOrdersOrderedByPlacedOn() {
        List<ProductOrder> productOrders = productOrderService.getAllProductOrdersOrderedByPlacedOnDesc();

        List<ProductOrderDTOOutput> ProductOrderDTOsOutput = new ArrayList<>();

        productOrders.forEach(productOrder -> {
            ProductOrderDTOOutput productOrderDTOOutput = convertProductOrderToProductOrderDTOOutput(productOrder);
            ProductOrderDTOsOutput.add(productOrderDTOOutput);
        });

        return ProductOrderDTOsOutput;
    }

    private ProductOrderDTOOutput convertProductOrderToProductOrderDTOOutput(ProductOrder productOrder) {
        ProductOrderDTOOutput productOrderDTOOutput = new ProductOrderDTOOutput();
        productOrderDTOOutput.setId(productOrder.getId());
        productOrderDTOOutput.setPlacedOn(productOrder.getPlacedOn());

        productOrder.getOrderItems().forEach(orderItem -> {
            Product product = orderItem.getProduct();
            ProductDTO productDTO = productService.convertProductToProductDTO(product);
            productOrderDTOOutput.addProductDTO(productDTO);
        });

        return productOrderDTOOutput;
    }

    @PostMapping
    public ProductOrderDTOOutput addOrder(@RequestBody ProductOrderDTOInput productOrderDTOInput) {

        if (productOrderDTOInput == null || productOrderDTOInput.getProductIds().size() == 0) {
            throw new ProductOrderNoOrderItemsException("Order is empty. Order has to contain at least one product.");
        }

        ProductOrder productOrder = convertProductOrderDTOInputToProductOrder(productOrderDTOInput);

        ProductOrder productOrderWithSetId = productOrderService.saveProductOrder(productOrder);
        return convertProductOrderToProductOrderDTOOutput(productOrderWithSetId);
    }

    private ProductOrder convertProductOrderDTOInputToProductOrder(ProductOrderDTOInput productOrderDTOInput) {
        ProductOrder productOrder = new ProductOrder();
        productOrder.setPlacedOn(Instant.now());

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
