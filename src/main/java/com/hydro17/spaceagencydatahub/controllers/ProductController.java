package com.hydro17.spaceagencydatahub.controllers;

import com.hydro17.spaceagencydatahub.exceptions.ProductErrorResponse;
import com.hydro17.spaceagencydatahub.exceptions.ProductNotFoundException;
import com.hydro17.spaceagencydatahub.exceptions.ProductNullFieldException;
import com.hydro17.spaceagencydatahub.models.Mission;
import com.hydro17.spaceagencydatahub.models.MissionDTO;
import com.hydro17.spaceagencydatahub.models.Product;
import com.hydro17.spaceagencydatahub.models.ProductDTO;
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
import java.util.ArrayList;
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
    public List<ProductDTO> getAllProducts() {
        List<Product> products = productService.getAllProducts();

        List<ProductDTO> productDTOs = new ArrayList<>();
        products.forEach(product -> productDTOs.add(convertProductToProductDTO(product)));

        return productDTOs;
    }

    @GetMapping("/{id}")
    public ProductDTO getProductById(@PathVariable long id) {
        Product product = productService.getProductById(id);

        if (product == null) {
            throw new ProductNotFoundException("There is no product with id: " + id);
        }

        return convertProductToProductDTO(product);
    }

    private ProductDTO convertProductToProductDTO(Product product) {
        ProductDTO productDTO = new ProductDTO();

        productDTO.setId(product.getId());
//        CHANGE THIS
        productDTO.setMissionName(product.getMissionName());
        productDTO.setAcquisitionDate(product.getAcquisitionDate());
        productDTO.setFootprint(product.getFootprint());
        productDTO.setPrice(product.getPrice());
        productDTO.setUrl(product.getUrl());

        return productDTO;
    }

    @GetMapping("/find")
    public List<Product> findProduct(@RequestParam(required = false) String missionName,
                            @RequestParam(required = false) ImageryType imageryType,
                            @RequestParam(required = false) Double latitude,
                            @RequestParam(required = false) Double longitude,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime beforeDate,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime afterDate) {
            logger.info(">>>>>>>>>> missionName : " + missionName);
            logger.info(">>>>>>>>>> imageryType : " + imageryType);
            logger.info(">>>>>>>>>> beforeDate : " + beforeDate);
            logger.info(">>>>>>>>>> latitude : " + latitude);
            logger.info(">>>>>>>>>> longitude : " + longitude);

            return productService.getFilteredProducts(missionName, beforeDate, afterDate, latitude, longitude);
    }

    @PostMapping
    public ProductDTO addProduct(@Valid @RequestBody ProductDTO productDTO, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new ProductNullFieldException("One of fields of the Product object is null");
        }

        Product productWithSetId = productService.saveProduct(convertProductDTOToProduct(productDTO));
        return convertProductToProductDTO(productWithSetId);
    }

    private Product convertProductDTOToProduct(ProductDTO productDTO) {
        Product product = new Product();

        product.setId(productDTO.getId());
//        CHANGE THIS
        product.setMissionName(productDTO.getMissionName());
        product.setAcquisitionDate(productDTO.getAcquisitionDate());
        product.setFootprint(productDTO.getFootprint());
        product.setPrice(productDTO.getPrice());
        product.setUrl(productDTO.getUrl());

        return product;
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
