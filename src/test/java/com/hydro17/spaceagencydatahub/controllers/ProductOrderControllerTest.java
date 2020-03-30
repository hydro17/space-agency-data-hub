package com.hydro17.spaceagencydatahub.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hydro17.spaceagencydatahub.exceptions.ErrorResponse;
import com.hydro17.spaceagencydatahub.models.*;
import com.hydro17.spaceagencydatahub.services.MissionService;
import com.hydro17.spaceagencydatahub.services.ProductOrderService;
import com.hydro17.spaceagencydatahub.services.ProductService;
import com.hydro17.spaceagencydatahub.utils.ImageryType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProductOrderController.class)
@WithMockUser(roles = "CUSTOMER")
class ProductOrderControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ProductOrderService productOrderService;

    // Added due to CommandLineRunner in the class SpaceAgencyDataHubApplication
    @MockBean
    private MissionService missionService;

    // Added due to CommandLineRunner in the class SpaceAgencyDataHubApplication
    @MockBean
    private ProductService productService;

    private List<ProductOrder> nonEmptyProductOrderList;

    private ProductOrder productOrder;
    private ProductOrderDTO nonEmptyProductOrderDTO;

    @BeforeEach
    void setUp() {
        Mission mission = new Mission();
        mission.setName("mission");
        mission.setImageryType(ImageryType.HYPERSPECTRAL);
        mission.setStartDate(LocalDateTime.now());
        mission.setFinishDate(LocalDateTime.now().plusHours(1L));

        ProductFootprint footprint = new ProductFootprint();
        footprint.setStartCoordinateLatitude(100.15);
        footprint.setEndCoordinateLatitude(200.99);
        footprint.setStartCoordinateLongitude(10.5);
        footprint.setEndCoordinateLongitude(50.7);

        Product product = new Product();
        product.setAcquisitionDate(LocalDateTime.now());
        product.setFootprint(footprint);
        product.setPrice(new BigDecimal("10.5"));
        product.setUrl("http://com1");
        product.setMission(mission);
        mission.addProduct(product);

        productOrder = new ProductOrder();
        productOrder.setPlacedOn(LocalDateTime.now());
        productOrder.addProduct(product);

        nonEmptyProductOrderList = new ArrayList<>();
        nonEmptyProductOrderList.add(productOrder);

        nonEmptyProductOrderDTO = new ProductOrderDTO();
        nonEmptyProductOrderDTO.setProductIds(Arrays.asList(1L, 2L, 3L));
    }

    @Test
    void getAllProductOrdersOrderedByPlacedOnDesc_whenValidInput_thenReturns200AndNonEmptyProductOrderList() throws Exception {

        when(productOrderService.getAllProductOrdersOrderedByPlacedOnDesc()).thenReturn(nonEmptyProductOrderList);

        MvcResult mvcResult = mockMvc.perform(get("/api/orders/history")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        String expectedResponsBody = objectMapper.writeValueAsString(nonEmptyProductOrderList);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualTo(expectedResponsBody);
    }

    @Test
    void addOrder_whenValidInput_thenReturns200AndProductOrder() throws Exception {

        when(productOrderService.convertProductOrderDTOToProductOrder(nonEmptyProductOrderDTO)).thenReturn(productOrder);
        when(productOrderService.saveProductOrder(productOrder)).thenReturn(productOrder);

        MvcResult mvcResult = mockMvc.perform(post("/api/orders")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(nonEmptyProductOrderDTO)))
                .andExpect(status().isOk())
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(productOrder);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualTo(expectedResponseBody);
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

    @WithMockUser(roles = "CONTENT_MANAGER")
    @Test
    void addOrder_whenValidInputAndInvalidRole_thenReturns403() throws Exception {

        mockMvc.perform(post("/api/orders")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(nonEmptyProductOrderDTO)))
                .andExpect(status().isForbidden());
    }
}