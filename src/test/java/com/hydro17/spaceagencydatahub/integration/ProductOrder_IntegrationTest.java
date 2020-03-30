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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@WithMockUser(roles = "CUSTOMER")
public class ProductOrder_IntegrationTest {

    @Autowired
    ProductOrderRepository productOrderRepository;

    @Autowired
    private MissionRepository missionRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    private List<ProductOrder> nonEmptyProductOrderList;

    private ProductOrder productOrder;
    private Mission mission;
    private Product product;

    @BeforeEach
    void setUp() {
        productOrderRepository.deleteAll();
        productRepository.deleteAll();
        missionRepository.deleteAll();

        mission = new Mission();
        mission.setName("mission");
        mission.setImageryType(ImageryType.HYPERSPECTRAL);
        mission.setStartDate(LocalDateTime.now());
        mission.setFinishDate(LocalDateTime.now().plusHours(1L));

        ProductFootprint footprint = new ProductFootprint();
        footprint.setStartCoordinateLatitude(100.15);
        footprint.setEndCoordinateLatitude(200.99);
        footprint.setStartCoordinateLongitude(10.5);
        footprint.setEndCoordinateLongitude(50.7);

        product = new Product();
        product.setAcquisitionDate(LocalDateTime.now());
        product.setFootprint(footprint);
        product.setPrice(new BigDecimal("10.5"));
        product.setUrl("http://com1");
        product.setMission(mission);
        mission.addProduct(product);

        productOrder = new ProductOrder();
        productOrder.setPlacedOn(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS));
        productOrder.addProduct(product);

        nonEmptyProductOrderList = new ArrayList<>();
        nonEmptyProductOrderList.add(productOrder);
    }

    @Test
    void getAllProductOrdersOrderedByPlacedOnDesc_whenValidInput_thenReturns200AndNonEmptyProductOrderList() throws Exception {

        missionRepository.save(mission);
        productRepository.save(product);
        productOrderRepository.save(productOrder);

        MvcResult mvcResult = mockMvc.perform(get("/api/orders/history")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        String expectedResponsBody = objectMapper.writeValueAsString(nonEmptyProductOrderList);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualTo(expectedResponsBody);
    }

    @Test
    void getAllProductOrdersOrderedByPlacedOnDesc_whenValidInput_thenReturns200AndEmptyProductOrderList() throws Exception {

        MvcResult mvcResult = mockMvc.perform(get("/api/orders/history")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        String expectedResponsBody = objectMapper.writeValueAsString(new ArrayList<ProductOrder>());
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualTo(expectedResponsBody);
    }

    @Test
    void addOrder_whenValidInput_thenReturns200() throws Exception {

        missionRepository.save(mission);
        Product savedProduct = productRepository.save(product);
        long savedProductId = savedProduct.getId();

        ProductOrderDTO nonEmptyProductOrderDTO = new ProductOrderDTO();
        nonEmptyProductOrderDTO.setProductIds(Arrays.asList(savedProductId));

        MvcResult mvcResult = mockMvc.perform(post("/api/orders")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(nonEmptyProductOrderDTO)))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    void addOrder_whenEmptyProductOrderDTO_thenReturns400() throws Exception {

        MvcResult mvcResult = mockMvc.perform(post("/api/orders")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(new ProductOrderDTO())))
                .andExpect(status().isBadRequest())
                .andReturn();

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setMessage("Order is empty. Order has to contain at least one product.");

        String expectedResponseBody = objectMapper.writeValueAsString(errorResponse);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualTo(expectedResponseBody);
    }
}
