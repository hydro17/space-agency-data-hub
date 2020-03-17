package com.hydro17.spaceagencydatahub.controllers;

import com.hydro17.spaceagencydatahub.exceptions.*;
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
        products.forEach(product -> productDTOs.add(productService.convertProductToProductDTO(product)));

        return productDTOs;
    }

    @GetMapping("/{id}")
    public ProductDTO getProductById(@PathVariable long id) {
        Product product = productService.getProductById(id);

        if (product == null) {
            throw new ProductNotFoundException("There is no product with id: " + id);
        }

        return productService.convertProductToProductDTO(product);
    }

    @GetMapping("/find")
    public List<ProductDTO> findProduct(@RequestParam(required = false) String missionName,
                            @RequestParam(required = false) ImageryType imageryType,
                            @RequestParam(required = false) Double latitude,
                            @RequestParam(required = false) Double longitude,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime beforeDate,
                            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime afterDate) {

//        logger.info(">>>>>>>>>> missionName : " + missionName);
//        logger.info(">>>>>>>>>> imageryType : " + imageryType);
//        logger.info(">>>>>>>>>> beforeDate : " + beforeDate);
//        logger.info(">>>>>>>>>> latitude : " + latitude);
//        logger.info(">>>>>>>>>> longitude : " + longitude);

        List<Product> products = productService.getFilteredProducts(missionName, beforeDate, afterDate, latitude, longitude, imageryType);

        List<ProductDTO> productDTOs = new ArrayList<>();
        products.forEach(product -> productDTOs.add(productService.convertProductToProductDTO(product)));

        return productService.removeUrlOfUnorderedProducts(productDTOs);
    }

    @PostMapping
    public ProductDTO addProduct(@Valid @RequestBody ProductDTO productDTO, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new ProductNullFieldException("One of fields of the Product object is null");
        }

        if (productService.doesMissionExist(productDTO.getMissionName()) == false) {
            throw new MissionNotFoundException("There is no mission with name: " + productDTO.getMissionName());
        }

        Product productWithSetId = productService.saveProduct(productService.convertProductDTOToProduct(productDTO));
        return productService.convertProductToProductDTO(productWithSetId);
    }

    @DeleteMapping("/{id}")
    public void deleteProductById(@PathVariable long id) {

        if (productService.getProductById(id) == null) {
            throw new ProductNotFoundException("There is no product with id:" + id);
        }

        productService.deleteProductById(id);
    }
}
