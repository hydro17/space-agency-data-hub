package com.hydro17.spaceagencydatahub.services;

import com.hydro17.spaceagencydatahub.models.Mission;
import com.hydro17.spaceagencydatahub.models.Product;
import com.hydro17.spaceagencydatahub.models.ProductDTO;
import com.hydro17.spaceagencydatahub.models.ProductFootprint;
import com.hydro17.spaceagencydatahub.repositories.ProductRepository;
import com.hydro17.spaceagencydatahub.utils.ImageryType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = ProductService.class)
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @MockBean
    private ProductRepository productRepository;

    @MockBean
    private MissionService missionService;

    @MockBean
    private ProductOrderService productOrderService;

    private List<Product> nonEmptyListOfProducts;
    private List<Product> emptyListOfProducts;

    Product product;
    Mission mission;
    ProductDTO productDTO;

    @BeforeEach
    void setUp() {
        emptyListOfProducts = new ArrayList<>();

        mission = new Mission();
        mission.setId(1L);
        mission.setName("mission1");
        mission.setImageryType(ImageryType.HYPERSPECTRAL);
        mission.setStartDate(LocalDateTime.now());
        mission.setFinishDate(LocalDateTime.now().plusHours(1L));

        ProductFootprint footprint = new ProductFootprint();
        footprint.setStartCoordinateLatitude(100.15);
        footprint.setEndCoordinateLatitude(200.99);
        footprint.setStartCoordinateLongitude(10.5);
        footprint.setEndCoordinateLongitude(50.7);

        product = new Product();
        product.setId(1L);
        product.setAcquisitionDate(LocalDateTime.now());
        product.setFootprint(footprint);
        product.setPrice(new BigDecimal("10.5"));
        product.setUrl("http://com");
        product.setMission(mission);
        mission.addProduct(product);

        nonEmptyListOfProducts = new ArrayList<>();
        nonEmptyListOfProducts.add(product);

        productDTO = new ProductDTO();
        productDTO.setMissionName("mission1");
        productDTO.setAcquisitionDate(LocalDateTime.now());
        productDTO.setPrice(new BigDecimal("10.7"));
        productDTO.setUrl("http://com");
        productDTO.setFootprint(footprint);
    }

    // -------------------------------------------------------------------------------

    @Test
    void getAllProducts_whenValidInput_thenReturnsNonEmptyListOfProducts() {
        when(productRepository.findAll()).thenReturn(nonEmptyListOfProducts);

        List<Product> actualOutput = productService.getAllProducts();

        assertThat(actualOutput).isEqualTo(nonEmptyListOfProducts);
    }

    @Test
    void getAllProducts_whenValidInput_thenReturnsEmptyListOfProducts() {
        when(productRepository.findAll()).thenReturn(emptyListOfProducts);

        List<Product> actualOutput = productService.getAllProducts();

        assertThat(actualOutput).isEqualTo(emptyListOfProducts);
    }

    @Test
    void getProductById_whenValidInput_thenReturnsProduct() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.ofNullable(product));

        Product actualOutput = productService.getProductById(anyLong());

        assertThat(actualOutput).isEqualTo(product);
    }

    @Test
    void getProductById_whenIdOfNonExistentProduct_thenReturnsNull() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        Product actualOutput = productService.getProductById(anyLong());

        assertThat(actualOutput).isNull();
    }

    @Test
    void getFilteredProducts_whenValidInput_thenReturnsNonEmptyListofPorducts() {
        when(productRepository.findAll(any(Specification.class))).thenReturn(nonEmptyListOfProducts);

        List<Product> actualOutput = productService.getFilteredProducts("mission1", LocalDateTime.now(),
                LocalDateTime.now().plusHours(1),100.2, 50.7, ImageryType.HYPERSPECTRAL);

        assertThat(actualOutput).isEqualTo(nonEmptyListOfProducts);
    }

    // -------------------------------------------------------------------------------

    @Test
    void saveProduct_whenProduct_thenReturnsProduct() {
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product actualOutput = productService.saveProduct(product);

        assertThat(actualOutput).isEqualTo(product);
    }

    // -------------------------------------------------------------------------------

    @Test
    void deleteProductById_whenValidInput_thenReturnsVoid() {

        productService.deleteProductById(anyLong());

        verify(productRepository, times(1)).deleteById(anyLong());
    }

    // -------------------------------------------------------------------------------

    @Test
    void doesMissionExist_whenMissionExists_thenReturnsTrue() {
        when(missionService.getMissionByName(anyString())).thenReturn(Optional.ofNullable(mission));

        Boolean actualOutput = productService.doesMissionExist("mission1");

        assertThat(actualOutput).isTrue();
    }

    @Test
    void doesMissionExist_whenMissionDoesNotExist_thenReturnsFalse() {
        when(missionService.getMissionByName(anyString())).thenReturn(Optional.empty());

        Boolean actualOutput = productService.doesMissionExist("mission1");

        assertThat(actualOutput).isFalse();
    }

    @Test
    void convertProductDTOToProduct() {
        when(missionService.getMissionByName(anyString())).thenReturn(Optional.ofNullable(mission));

        Product actualOutput = productService.convertProductDTOToProduct(productDTO);

        assertThat(actualOutput.getId()).isZero();
        assertThat(actualOutput.getMission().getName()).isEqualTo(productDTO.getMissionName());
        assertThat(actualOutput.getAcquisitionDate()).isEqualTo(productDTO.getAcquisitionDate());
        assertThat(actualOutput.getFootprint()).isEqualTo(productDTO.getFootprint());
        assertThat(actualOutput.getPrice()).isEqualTo(productDTO.getPrice());
        assertThat(actualOutput.getUrl()).isEqualTo(productDTO.getUrl());
    }

    @Test
    void removeUrlOfUnorderedProducts_whenProductOrdered_thenDoNotRemoveUrl() {
        when(productOrderService.isOrderedProductById(anyLong())).thenReturn(true);

        List<Product> actualOutput = productService.removeUrlOfUnorderedProducts(nonEmptyListOfProducts);

        assertThat(actualOutput.get(0).getUrl()).isNotNull();
    }

    @Test
    void removeUrlOfUnorderedProducts_whenProductIsNotOrdered_thenRemoveUrl() {
        when(productOrderService.isOrderedProductById(anyLong())).thenReturn(false);

        List<Product> actualOutput = productService.removeUrlOfUnorderedProducts(nonEmptyListOfProducts);

        assertThat(actualOutput).isEqualTo(nonEmptyListOfProducts);
        assertThat(actualOutput.get(0).getUrl()).isNull();
    }
}