package com.hydro17.spaceagencydatahub.controllers;

import com.hydro17.spaceagencydatahub.exceptions.MissionNotFoundException;
import com.hydro17.spaceagencydatahub.exceptions.ProductIsOrderedException;
import com.hydro17.spaceagencydatahub.exceptions.ProductNotFoundException;
import com.hydro17.spaceagencydatahub.exceptions.ProductNullFieldException;
import com.hydro17.spaceagencydatahub.models.IProductAndOrderCount;
import com.hydro17.spaceagencydatahub.models.Product;
import com.hydro17.spaceagencydatahub.models.ProductDTO;
import com.hydro17.spaceagencydatahub.services.OrderItemService;
import com.hydro17.spaceagencydatahub.services.ProductOrderService;
import com.hydro17.spaceagencydatahub.services.ProductService;
import com.hydro17.spaceagencydatahub.utils.ImageryType;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private ProductService productService;
    private OrderItemService orderItemService;
    private ProductOrderService productOrderService;

    public ProductController(ProductService productService, OrderItemService orderItemService,
                             ProductOrderService productOrderService) {
        this.productService = productService;
        this.orderItemService = orderItemService;
        this.productOrderService = productOrderService;
    }

    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public Product getProductById(@PathVariable long id) {
        Product product = productService.getProductById(id);

        if (product == null) {
            throw new ProductNotFoundException("There is no product with id: " + id);
        }

        return product;
    }

    @GetMapping("/find")
    public List<Product> findProduct(
            @RequestParam(required = false) String missionName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime beforeDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime afterDate,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) ImageryType imageryType
    ) {
        List<Product> products = productService.getFilteredProducts(missionName, beforeDate, afterDate, latitude, longitude, imageryType);
        return productService.removeUrlOfUnorderedProducts(products);
    }

    @GetMapping("/most-ordered")
    public List<Product> getProductsGroupedByProductIdOrderedByOrderCountDesc() {
        List<IProductAndOrderCount> productAndProductOrderCounts =
                orderItemService.getAllProductAndOrderCountGroupedByProductIdOrderedByOrderCountDesc();

        List<Product> products = orderItemService.convertAllProductAndOrderCountToProduct(productAndProductOrderCounts);
        return products;
    }

    @PostMapping
    public Product addProduct(@Valid @RequestBody ProductDTO productDTO, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new ProductNullFieldException("No product field can be null");
        }

        if (productService.doesMissionExist(productDTO.getMissionName()) == false) {
            throw new MissionNotFoundException("There is no product with name: " + productDTO.getMissionName());
        }

        Product productWithSetId = productService.saveProduct(productService.convertProductDTOToProduct(productDTO));
        return productWithSetId;
    }

    @DeleteMapping("/{id}")
    public void deleteProductById(@PathVariable long id) {

        if (productService.getProductById(id) == null) {
            throw new ProductNotFoundException("There is no product with id:" + id);
        }

        if (productOrderService.isOrderedProductById(id)) {
            throw new ProductIsOrderedException("Product with id " + id + " is ordered and can't be removed");
        }

        productService.deleteProductById(id);
    }
}
