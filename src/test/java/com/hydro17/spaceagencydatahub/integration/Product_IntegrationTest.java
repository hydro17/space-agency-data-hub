package com.hydro17.spaceagencydatahub.integration;

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
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@WithMockUser(roles="CONTENT_MANAGER")
public class Product_IntegrationTest {

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
    private List<ProductDTO> nonEmptyListOfProductDTOs;

    private Product product;
    private ProductDTO productDTO;
    private ProductDTO productDTOWithIdNotEqualZero;
    private Mission mission;
    private ProductOrder productOrder;

    @BeforeEach
    void setUp() {
        productOrderRepository.deleteAll();
        productRepository.deleteAll();
        missionRepository.deleteAll();

        mission = new Mission();
        mission.setName("mission1");
        mission.setImageryType(ImageryType.HYPERSPECTRAL);
        mission.setStartDate(LocalDateTime.now().minusHours(1L));
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
        productDTO.setMissionName(product.getMission().getName());
        productDTO.setAcquisitionDate(product.getAcquisitionDate());
        productDTO.setFootprint(product.getFootprint());
        productDTO.setPrice(product.getPrice());
        productDTO.setUrl(product.getUrl());

        productDTOWithIdNotEqualZero = new ProductDTO();
        productDTOWithIdNotEqualZero.setId(product.getId());
        productDTOWithIdNotEqualZero.setMissionName(product.getMission().getName());
        productDTOWithIdNotEqualZero.setAcquisitionDate(product.getAcquisitionDate());
        productDTOWithIdNotEqualZero.setFootprint(product.getFootprint());
        productDTOWithIdNotEqualZero.setPrice(product.getPrice());
        productDTOWithIdNotEqualZero.setUrl(product.getUrl());

        nonEmptyListOfProductDTOs = new ArrayList<>();
        nonEmptyListOfProductDTOs.add(productDTOWithIdNotEqualZero);

        emptyListOfProducts = new ArrayList<>();

        nonEmptyListOfProducts = new ArrayList<>();
        nonEmptyListOfProducts.add(product);

        productOrder = new ProductOrder();
        productOrder.setPlacedOn(LocalDateTime.now());
        productOrder.addProduct(product);
    }

    //  ----------------------------------------------------------------------------------------------

    @Test
    void getAllProducts_whenValidInput_thenReturns200AndNonEmptyListOfProductDTOs() throws Exception {

        // mission must be added to DB before adding to DB product belonging to this mission
        missionRepository.save(mission);
        productRepository.save(product);

        MvcResult mvcResult = mockMvc.perform(get("/api/products")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        ProductDTO expectedOutput = nonEmptyListOfProductDTOs.get(0);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();
        ProductDTO actualOutput = Arrays.asList(objectMapper.readValue(actualResponseBody, ProductDTO[].class)).get(0);

        assertThat(actualOutput).isEqualToIgnoringGivenFields(expectedOutput, "id", "footprint");
        assertThat(actualOutput.getFootprint()).isEqualToIgnoringGivenFields(expectedOutput.getFootprint(), "id");
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
    void getProductById_whenValidInput_thenReturns200AndProductDTO() throws Exception {

        // mission must be added to DB before adding to DB product belonging to this mission
        missionRepository.save(mission);
        long productId = productRepository.save(product).getId();

        MvcResult mvcResult = mockMvc.perform(get("/api/products/{id}", productId)
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        ProductDTO expectedOutput = productDTOWithIdNotEqualZero;
        String actualResponseBody = mvcResult.getResponse().getContentAsString();
        ProductDTO actualOutput = objectMapper.readValue(actualResponseBody, ProductDTO.class);

        assertThat(actualOutput).isEqualToIgnoringGivenFields(expectedOutput, "id", "footprint");
        assertThat(actualOutput.getFootprint()).isEqualToIgnoringGivenFields(expectedOutput.getFootprint(), "id");
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

        assertThat(actualResponseBody).isEqualTo(expectedResponseBody);
    }

    //  ----------------------------------------------------------------------------------------------

    //getProductsGroupedByProductIdOrderedByOrderCountDesc
    @Test
    void getMostOrderedProductsDesc_whenValidInput_returns200AndNotEmptyListOfProductDTOs() throws Exception {

        // mission must be added to DB before adding to DB product belonging to this mission
        missionRepository.save(mission);
        // product must be added to DB before adding to DB product order containing this product
        productRepository.save(product);
        productOrderRepository.save(productOrder);

        MvcResult mvcResult = mockMvc.perform(get("/api/products/most-ordered")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        ProductDTO expectedOutput = nonEmptyListOfProductDTOs.get(0);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();
        ProductDTO actualOutput = Arrays.asList(objectMapper.readValue(actualResponseBody, ProductDTO[].class)).get(0);

        assertThat(actualOutput).isEqualToIgnoringGivenFields(expectedOutput, "id", "footprint");
        assertThat(actualOutput.getFootprint()).isEqualToIgnoringGivenFields(expectedOutput.getFootprint(), "id");
    }

    //getProductsGroupedByProductIdOrderedByOrderCountDesc
    @Test
    void getMostOrderedProductsDesc_whenValidInput_returns200AndEmptyListOfProducts() throws Exception {

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
    void addProduct_whenValidInput_thenReturns201AndMission() throws Exception {

        missionRepository.save(mission);

        MvcResult mvcResult = mockMvc.perform(post("/api/products")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isCreated())
                .andReturn();

        String actualResponseBody =  mvcResult.getResponse().getContentAsString();
        Product responseAsProduct = objectMapper.readValue(actualResponseBody, Product.class);

        productDTO.getFootprint().setId(responseAsProduct.getFootprint().getId());

        assertThat(responseAsProduct).isEqualToIgnoringGivenFields(productDTO, "id", "mission");
        assertThat(productDTO.getId()).isZero();
        assertThat(responseAsProduct.getId()).isNotZero();
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
        errorResponse.setMessage("Product fields: 'price' must not be null");

        String expectedResponseBody = objectMapper.writeValueAsString(errorResponse);
        String actualResponseBody =  mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualTo(expectedResponseBody);
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
        errorResponse.setMessage("There is no mission with the name: " + productDTO.getMissionName());

        String expectedResponseBody = objectMapper.writeValueAsString(errorResponse);
        String actualResponseBody =  mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualTo(expectedResponseBody);
    }

//  ----------------------------------------------------------------------------------------------

    @Test
    void deleteProductById_whenValidInput_thenReturns204() throws Exception {

        missionRepository.save(mission);
        Product savedProduct = productRepository.save(product);
        long productId = savedProduct.getId();

        mockMvc.perform(delete("/api/products/{id}", productId))
                .andExpect(status().isNoContent());
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

        assertThat(actualResponseBody).isEqualTo(expectedResponseBody);
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

        assertThat(actualResponseBody).isEqualTo(expectedResponseBody);
    }
}
