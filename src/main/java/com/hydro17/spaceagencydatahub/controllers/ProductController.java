package com.hydro17.spaceagencydatahub.controllers;

import com.hydro17.spaceagencydatahub.exceptions.ProductErrorResponse;
import com.hydro17.spaceagencydatahub.exceptions.ProductNotFoundException;
import com.hydro17.spaceagencydatahub.exceptions.ProductNullFieldException;
import com.hydro17.spaceagencydatahub.models.Product;
import com.hydro17.spaceagencydatahub.services.ProductService;
import com.hydro17.spaceagencydatahub.utils.ImageryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private ProductService productService;
    private Logger logger = LoggerFactory.getLogger(ProductController.class);

    public ProductController(ProductService productService) {
        this.productService = productService;
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
    public List<Product> findProduct(@RequestParam(required = false) String missionName,
                            @RequestParam(required = false) ImageryType imageryType,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime firstDate,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime secondDate) {
            logger.info(">>>>>>>>>> missionName : " + missionName);
            logger.info(">>>>>>>>>> imageryType : " + imageryType);
            logger.info(">>>>>>>>>> firstDate : " + firstDate);
            logger.info(">>>>>>>>>> secondDate : " + secondDate);

            if (missionName != null) {
                if (firstDate != null && secondDate == null) {
                   return productService.getAllProductsByMissionNameAndBeforeAcquisitionDate(missionName, firstDate);
                }

                if (firstDate == null && secondDate != null) {
                    return productService.getAllProductsByMissionNameAndAfterAcquisitionDate(missionName, secondDate);
                }

                if (firstDate != null && secondDate != null) {
                    
                }

                return productService.getAllProductsByMissionName(missionName);
            }

            return null;
    }

    @PostMapping
    public Product addProduct(@Valid @RequestBody Product product, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new ProductNullFieldException("One of fields of the Product object is null");
        }

        Product productWithSetId = productService.saveProduct(product);
        return productWithSetId;
    }

    @DeleteMapping("/{id}")
    public void deleteProductById(@PathVariable long id) {

        if (productService.getProductById(id) == null) {
            throw new ProductNotFoundException("There is no product with id:" + id);
        }

        productService.deleteProductById(id);
    }

    @ExceptionHandler
    public ResponseEntity<ProductErrorResponse> handleException(ProductNotFoundException ex) {

        ProductErrorResponse error = new ProductErrorResponse();
        error.setStatus(HttpStatus.NOT_FOUND.value());
        error.setMessage(ex.getMessage());

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<ProductErrorResponse> handleException(ProductNullFieldException ex) {

        ProductErrorResponse error = new ProductErrorResponse();
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setMessage(ex.getMessage());

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<String> handleAllExceptions(Exception ex, WebRequest request) {
        return new ResponseEntity<>("|" + ex.toString() + "|", HttpStatus.BAD_REQUEST);
    }
}
