package com.hydro17.spaceagencydatahub.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hydro17.spaceagencydatahub.exceptions.ErrorResponse;
import com.hydro17.spaceagencydatahub.models.*;
import com.hydro17.spaceagencydatahub.repositories.MissionRepository;
import com.hydro17.spaceagencydatahub.repositories.ProductOrderRepository;
import com.hydro17.spaceagencydatahub.repositories.ProductRepository;
import com.hydro17.spaceagencydatahub.utils.ImageryType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@WithMockUser(roles="CONTENT_MANAGER")
public class ProductController_IntegrationTest {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductOrderRepository productOrderRepository;

    @Autowired
    MissionRepository missionRepository;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    private List<Product> emptyListOfProducts;
    private List<Product> nonEmptyListOfProducts;

    Product product;
    ProductDTO productDTO;
    Mission mission;
    ProductOrder productOrder;

    @BeforeEach
    void setUp() {
        productOrderRepository.deleteAll();
        productRepository.deleteAll();
        missionRepository.deleteAll();

        mission = new Mission();
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
        product.setAcquisitionDate(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS));
        product.setFootprint(footprint);
        product.setPrice(new BigDecimal("10.50"));
        product.setUrl("http://com");
        product.setMission(mission);
        mission.addProduct(product);

        productDTO = new ProductDTO();
        productDTO.setMissionName(product.getMissionName());
        productDTO.setAcquisitionDate(product.getAcquisitionDate());
        productDTO.setFootprint(product.getFootprint());
        productDTO.setPrice(product.getPrice());
        productDTO.setUrl(product.getUrl());

        emptyListOfProducts = new ArrayList<>();
        nonEmptyListOfProducts = new ArrayList<>();

        nonEmptyListOfProducts.add(product);

        productOrder = new ProductOrder();
        productOrder.setPlacedOn(LocalDateTime.now());
        productOrder.addProduct(product);
    }

    //  ----------------------------------------------------------------------------------------------

    @Test
    void getAllProducts_whenValidInput_thenReturns200AndNonEmptyListOfProducts() throws Exception {

        // mission must be added to DB before adding to DB product belonging to this mission
        missionRepository.save(mission);
        productRepository.save(product);

        MvcResult mvcResult = mockMvc.perform(get("/api/products")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(nonEmptyListOfProducts);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualTo(expectedResponseBody);
    }

    @Test
    void getAllProducts_whenValidInput_thenReturns200AndEmptyListOfProducts() throws Exception {

        MvcResult mvcResult = mockMvc.perform(get("/api/products")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(emptyListOfProducts);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualTo(expectedResponseBody);
    }

    @WithMockUser(roles = "CUSTOMER")
    @Test
    void getAllProducts_whenValidInputAndUserWithWrongRole_thenReturns403() throws Exception {

        mockMvc.perform(get("/api/products")
                .contentType("application/json"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getProductById_whenValidInput_thenReturns200AndMission() throws Exception {

        // mission must be added to DB before adding to DB product belonging to this mission
        missionRepository.save(mission);
        long productId = productRepository.save(product).getId();

        MvcResult mvcResult = mockMvc.perform(get("/api/products/{id}", productId)
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(product);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualTo(expectedResponseBody);
    }

    @Test
    void getProductById_whenIdOfNonExistingProduct_thenReturns404AndErrorResponse() throws Exception {

        // mission must be added to DB before adding to DB product belonging to this mission
        missionRepository.save(mission);
        long productId = productRepository.save(product).getId();
        productRepository.delete(product);

        MvcResult mvcResult = mockMvc.perform(get("/api/products/{id}", productId)
                .contentType("application/json"))
                .andExpect(status().isNotFound())
                .andReturn();

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(HttpStatus.NOT_FOUND.value());
        errorResponse.setMessage("There is no product with id: " + productId);

        String expectedResponseBody = objectMapper.writeValueAsString(errorResponse);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(expectedResponseBody);
    }

    //  ----------------------------------------------------------------------------------------------

    @WithMockUser(roles = "CUSTOMER")
    @Test
    void findProduct_whenValidInput_returns200AndEmptyListOfProducts() throws Exception {

        Mission mission = new Mission();
        mission.setName("mission1");
        mission.setImageryType(ImageryType.HYPERSPECTRAL);
        mission.setStartDate(LocalDateTime.now());
        mission.setFinishDate(LocalDateTime.now().plusHours(1L));

        ProductFootprint footprint = new ProductFootprint();
        footprint.setStartCoordinateLatitude(100.15);
        footprint.setEndCoordinateLatitude(200.99);
        footprint.setStartCoordinateLongitude(10.5);
        footprint.setEndCoordinateLongitude(50.7);

        Product product = new Product();
        product.setAcquisitionDate(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS));
        product.setFootprint(footprint);
        product.setPrice(new BigDecimal("10.50"));
        product.setUrl("http://com");
        product.setMission(mission);
        mission.addProduct(product);

        missionRepository.save(mission);
        productRepository.save(product);

//      Parameters / Query string
        String missionName = "mission1";
        LocalDateTime afterDate = LocalDateTime.now().minusHours(1L);
        LocalDateTime beforeDate = LocalDateTime.now().plusHours(2L);
        Double latitude = 150.3;
        Double longitude = 70.7;
        ImageryType imageryType = ImageryType.HYPERSPECTRAL;

        MvcResult mvcResult = mockMvc.perform(get("/api/products/find")
                .contentType("application/json")
                .param("missionName", missionName)
                .param("afterDate", String.valueOf(afterDate))
                .param("beforeDate", String.valueOf(beforeDate))
                .param("latitude", String.valueOf(latitude))
                .param("longitude", String.valueOf(longitude))
                .param("imageryType", String.valueOf(imageryType)))
                .andExpect(status().isOk())
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(emptyListOfProducts);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(expectedResponseBody);
    }

    @Test
    void findProduct_whenValidInputAndInvalidRole_returns403() throws Exception {

        String missionName = "mission1";
        LocalDateTime beforeDate = LocalDateTime.now();
        LocalDateTime afterDate = LocalDateTime.now().plusHours(1L);
        Double latitude = 10.3;
        Double longitude = 20.7;
        ImageryType imageryType = ImageryType.HYPERSPECTRAL;

        MvcResult mvcResult = mockMvc.perform(get("/api/products/find")
                .contentType("application/json")
                .param("missionName", missionName)
                .param("beforeDate", String.valueOf(beforeDate))
                .param("afterDate", String.valueOf(afterDate))
                .param("latitude", String.valueOf(latitude))
                .param("longitude", String.valueOf(longitude))
                .param("imageryType", String.valueOf(imageryType)))
                .andExpect(status().isForbidden())
                .andReturn();
    }

    // get01 = getProductsGroupedByProductIdOrderedByOrderCountDesc
    @Test
    void get01_whenValidInput_returns200AndNotEmptyListOfProducts() throws Exception {

        // mission must be added to DB before adding to DB product belonging to this mission
        missionRepository.save(mission);
        // product must be added to DB before adding to DB product order containing this product
        productRepository.save(product);
        productOrderRepository.save(productOrder);

        MvcResult mvcResult = mockMvc.perform(get("/api/products/most-ordered")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(nonEmptyListOfProducts);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualTo(expectedResponseBody);
    }

    // get01 = getProductsGroupedByProductIdOrderedByOrderCountDesc
    @Test
    void get01_whenValidInput_returns200AndEmptyListOfProducts() throws Exception {

        MvcResult mvcResult = mockMvc.perform(get("/api/products/most-ordered")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(emptyListOfProducts);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualTo(expectedResponseBody);
    }

    //  ----------------------------------------------------------------------------------------------

    @Test
    void addProduct_whenValidInput_thenReturns200AndMission() throws Exception {

        missionRepository.save(mission);

        MvcResult mvcResult = mockMvc.perform(post("/api/products")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isOk())
                .andReturn();

        String actualResponseBody =  mvcResult.getResponse().getContentAsString();
        Product responseAsProduct = objectMapper.readValue(actualResponseBody, Product.class);

        productDTO.getFootprint().setId(responseAsProduct.getFootprint().getId());

//      Mission name cannot be asserted because of JSON mapping
        assertThat(responseAsProduct.getAcquisitionDate()).isEqualTo(productDTO.getAcquisitionDate());
        assertThat(responseAsProduct.getFootprint()).isEqualTo(productDTO.getFootprint());
        assertThat(responseAsProduct.getPrice()).isEqualTo(productDTO.getPrice());
        assertThat(responseAsProduct.getUrl()).isEqualTo(productDTO.getUrl());
    }

    @Test
    void addProduct_whenProductFieldIsNull_thenReturns400AndErrorResponse() throws Exception {

        ProductDTO productDTOWithNullPriceField = productDTO;
        productDTOWithNullPriceField.setPrice(null);

        MvcResult mvcResult = mockMvc.perform(post("/api/products")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(productDTOWithNullPriceField)))
                .andExpect(status().isBadRequest())
                .andReturn();

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setMessage("No product field can be null");

        String expectedResponseBody = objectMapper.writeValueAsString(errorResponse);
        String actualResponseBody =  mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(expectedResponseBody);
    }

    @Test
    void addProduct_whenMissionDoesNotExist_thenReturns404AndErrorResponse() throws Exception {

        productDTO.setMissionName("mission abc");

        MvcResult mvcResult = mockMvc.perform(post("/api/products")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isNotFound())
                .andReturn();

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(HttpStatus.NOT_FOUND.value());
        errorResponse.setMessage("There is no mission with name: " + productDTO.getMissionName());

        String expectedResponseBody = objectMapper.writeValueAsString(errorResponse);
        String actualResponseBody =  mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(expectedResponseBody);
    }

//  ----------------------------------------------------------------------------------------------

    @Test
    void deleteProductById_whenValidInput_thenReturns200() throws Exception {

        missionRepository.save(mission);
        Product savedProduct = productRepository.save(product);
        long productId = savedProduct.getId();

        mockMvc.perform(delete("/api/products/{id}", productId))
                .andExpect(status().isOk());
    }

    @Test
    void deleteProductById_whenProductDoesNotExist_thenReturns404AndErrorResponse() throws Exception {

        missionRepository.save(mission);
        Product savedProduct = productRepository.save(product);
        long productId = savedProduct.getId();
        productRepository.delete(savedProduct);

        MvcResult mvcResult = mockMvc.perform(delete("/api/products/{id}", productId))
                .andExpect(status().isNotFound())
                .andReturn();

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(HttpStatus.NOT_FOUND.value());
        errorResponse.setMessage("There is no product with id:" + productId);

        String expectedResponseBody = objectMapper.writeValueAsString(errorResponse);
        String actualResponseBody =  mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(expectedResponseBody);
    }

    @Test
    void deleteProductById_whenProductIsOrdered_thenReturns400AndErrorResponse() throws Exception {

        // mission must be added to DB before adding to DB product belonging to this mission
        missionRepository.save(mission);
        // product must be added to DB before adding to DB product order containing this product
        Product savedProduct = productRepository.save(product);
        productOrderRepository.save(productOrder);
        long productId = savedProduct.getId();

        MvcResult mvcResult = mockMvc.perform(delete("/api/products/{id}", productId))
                .andExpect(status().isBadRequest())
                .andReturn();

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setMessage("Product with id " + productId + " is ordered and can't be removed");

        String expectedResponseBody = objectMapper.writeValueAsString(errorResponse);
        String actualResponseBody =  mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(expectedResponseBody);
    }
}
