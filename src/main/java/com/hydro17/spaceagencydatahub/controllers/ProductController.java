package com.hydro17.spaceagencydatahub.controllers;

import com.hydro17.spaceagencydatahub.exceptions.*;
import com.hydro17.spaceagencydatahub.models.IProductAndOrderCount;
import com.hydro17.spaceagencydatahub.models.Mission;
import com.hydro17.spaceagencydatahub.models.Product;
import com.hydro17.spaceagencydatahub.models.ProductDTO;
import com.hydro17.spaceagencydatahub.services.MissionService;
import com.hydro17.spaceagencydatahub.services.OrderItemService;
import com.hydro17.spaceagencydatahub.services.ProductOrderService;
import com.hydro17.spaceagencydatahub.services.ProductService;
import com.hydro17.spaceagencydatahub.utils.ImageryType;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private ProductService productService;
    private OrderItemService orderItemService;
    private ProductOrderService productOrderService;
    private MissionService missionService;
    private ConversionService conversionService;

    public ProductController(ProductService productService, OrderItemService orderItemService,
                             ProductOrderService productOrderService, MissionService missionService,
                             ConversionService conversionService) {
        this.productService = productService;
        this.orderItemService = orderItemService;
        this.productOrderService = productOrderService;
        this.missionService = missionService;
        this.conversionService = conversionService;
    }

    @GetMapping
    public List<ProductDTO> getAllProducts() {
        List<Product> products = productService.getAllProducts();

//        TODO remove after testing @MockBean conversionService
//        Long aa = conversionService.convert("10", Long.class);
//        products.get(0).setId(aa);

        return products.stream()
                .map(product -> conversionService.convert(product, ProductDTO.class))
                .collect(Collectors.toList());
    }

    //TODO refactor null -> optional
    @GetMapping("/{id}")
    public ProductDTO getProductById(@PathVariable long id) {
        Product product = productService.getProductById(id);

        if (product == null) {
            throw new ProductNotFoundException("There is no product with id: " + id);
        }

        return conversionService.convert(product, ProductDTO.class) ;
    }

    @GetMapping("/find")
    public List<ProductDTO> findProduct(
            @RequestParam(required = false) String missionName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime beforeDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime afterDate,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false, value = "imageryType") String imageryTypeAsString
    ) {
        ImageryType imageryType = null;

        if (imageryTypeAsString != null) {
            try {
                imageryType = ImageryType.valueOf(imageryTypeAsString.toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new ProductBadFindProductParameterException("Imagery type " + imageryTypeAsString + " does not exist");
            }
        }

        List<Product> products = productService.getFilteredProducts(missionName, beforeDate, afterDate, latitude,
                longitude, imageryType);
        List<Product> listOfProductsWithUrlNullForUnordered = productService.removeUrlOfUnorderedProducts(products);

        return listOfProductsWithUrlNullForUnordered.stream()
                .map(product -> conversionService.convert(product, ProductDTO.class))
                .collect(Collectors.toList());
    }

//    getProductsGroupedByProductIdOrderedByOrderCountDesc
    @GetMapping("/most-ordered")
    public List<ProductDTO> getMostOrderedProductsDesc() {
        List<IProductAndOrderCount> productAndProductOrderCounts =
                orderItemService.getAllProductAndOrderCountGroupedByProductIdOrderedByOrderCountDesc();

        List<Product> products = orderItemService.convertAllProductAndOrderCountToProduct(productAndProductOrderCounts);

        return products.stream()
                .map(product -> conversionService.convert(product, ProductDTO.class))
                .collect(Collectors.toList());
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ProductDTO addProduct(@Valid @RequestBody ProductDTO productDTO, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new ProductNullFieldException("No product field can be null");
        }

        Mission mission = missionService.getMissionByName(productDTO.getMissionName()).orElseThrow(() -> {
            throw new MissionNotFoundException("There is no mission with the name: " + productDTO.getMissionName());
        });

        if (productDTO.getAcquisitionDate().isBefore(mission.getStartDate())) {
            throw new ProductBadAcquisitionDateException("Product AcquisitionDate " + productDTO.getAcquisitionDate()
                    + " is before mission start date " + mission.getStartDate());
        }

        if (productDTO.getAcquisitionDate().isAfter(mission.getFinishDate())) {
            throw new ProductBadAcquisitionDateException("Product AcquisitionDate " + productDTO.getAcquisitionDate()
                    + " is after mission start date " + mission.getStartDate());
        }

        productDTO.setId(0);
        Product productWithSetId = productService.saveProduct(conversionService.convert(productDTO, Product.class));
        return conversionService.convert(productWithSetId, ProductDTO.class);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
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
