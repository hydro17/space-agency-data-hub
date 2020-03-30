package com.hydro17.spaceagencydatahub.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hydro17.spaceagencydatahub.exceptions.ErrorResponse;
import com.hydro17.spaceagencydatahub.models.*;
import com.hydro17.spaceagencydatahub.services.*;
import com.hydro17.spaceagencydatahub.utils.ImageryType;
import lombok.AllArgsConstructor;
import lombok.Getter;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProductController.class)
@WithMockUser(roles = "CONTENT_MANAGER")
class ProductControllerTest {

    @Getter
    @AllArgsConstructor
    private static class ProductAndOrderCount implements IProductAndOrderCount {
        private Product product;
        private Long orderCount;
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @MockBean
    private OrderItemService orderItemService;

    @MockBean
    private ProductOrderService productOrderService;

    // Added due to CommandLineRunner in the class SpaceAgencyDataHubApplication
    @MockBean
    private MissionService missionService;

    private List<Product> emptyListOfProducts;
    private List<Product> nonEmptyListOfProducts;
    private List<IProductAndOrderCount> emptyListOfProductAndOrderCounts;
    private List<IProductAndOrderCount> notEmptyListOfProductAndOrderCounts;

    private Product product1;
    private ProductDTO productDTO;
    private Mission mission;

    @BeforeEach
    void setUp() {
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

        product1 = new Product();
        product1.setAcquisitionDate(LocalDateTime.now());
        product1.setFootprint(footprint);
        product1.setPrice(new BigDecimal("10.5"));
        product1.setUrl("http://com1");
        product1.setMission(mission);
        mission.addProduct(product1);

        emptyListOfProducts = new ArrayList<>();
        nonEmptyListOfProducts = new ArrayList<>();

        nonEmptyListOfProducts.add(product1);

        productDTO = new ProductDTO();
        productDTO.setMissionName(product1.getMissionName());
        productDTO.setAcquisitionDate(product1.getAcquisitionDate());
        productDTO.setFootprint(product1.getFootprint());
        productDTO.setPrice(product1.getPrice());
        productDTO.setUrl(product1.getUrl());

        emptyListOfProductAndOrderCounts = new ArrayList<>();
        notEmptyListOfProductAndOrderCounts = new ArrayList<>();

        notEmptyListOfProductAndOrderCounts.add(new ProductAndOrderCount(product1, 1L));
    }

    //  ----------------------------------------------------------------------------------------------

    @Test
    void getAllProducts_whenValidInput_thenReturns200AndNonEmptyListOfProducts() throws Exception {

        when(productService.getAllProducts()).thenReturn(nonEmptyListOfProducts);

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

        when(productService.getAllProducts()).thenReturn(emptyListOfProducts);

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

        when(productService.getProductById(any(Long.class))).thenReturn(product1);

        MvcResult mvcResult = mockMvc.perform(get("/api/products/{id}", 1L)
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(product1);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(expectedResponseBody);
    }

    @Test
    void getProductById_whenIdOfNonExistingProduct_thenReturns404AndErrorResponse() throws Exception {

        long productId = 1L;
        when(productService.getProductById(any(Long.class))).thenReturn(null);

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

    @WithMockUser(roles = "CUSTOMER")
    @Test
    void findProduct_whenValidInput_returns200AndNonEmptyListOfProducts() throws Exception {

        String missionName = "mission1";
        LocalDateTime beforeDate = LocalDateTime.now();
        LocalDateTime afterDate = LocalDateTime.now().plusHours(1L);
        Double latitude = 10.3;
        Double longitude = 20.7;
        ImageryType imageryType = ImageryType.HYPERSPECTRAL;

        when(productService.getFilteredProducts(nullable(String.class), nullable(LocalDateTime.class), nullable(LocalDateTime.class),
                nullable(Double.class), nullable(Double.class), nullable(ImageryType.class))).thenReturn(nonEmptyListOfProducts);
        when(productService.removeUrlOfUnorderedProducts(nonEmptyListOfProducts)).thenReturn(nonEmptyListOfProducts);

        MvcResult mvcResult = mockMvc.perform(get("/api/products/find")
                .contentType("application/json")
                .param("missionName", missionName)
                .param("beforeDate", String.valueOf(beforeDate))
                .param("afterDate", String.valueOf(afterDate))
                .param("latitude", String.valueOf(latitude))
                .param("longitude", String.valueOf(longitude))
                .param("imageryType", String.valueOf(imageryType)))
                .andExpect(status().isOk())
                .andReturn();

        verify(productService, times(1)).getFilteredProducts(eq(missionName), eq(beforeDate),
                eq(afterDate), eq(latitude), eq(longitude), eq((imageryType)));

        String expectedResponseBody = objectMapper.writeValueAsString(nonEmptyListOfProducts);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualTo(expectedResponseBody);
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

        when(orderItemService.getAllProductAndOrderCountGroupedByProductIdOrderedByOrderCountDesc())
                .thenReturn(notEmptyListOfProductAndOrderCounts);
        when(orderItemService.convertAllProductAndOrderCountToProduct(notEmptyListOfProductAndOrderCounts))
                .thenReturn(nonEmptyListOfProducts);

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

        when(orderItemService.getAllProductAndOrderCountGroupedByProductIdOrderedByOrderCountDesc())
                .thenReturn(emptyListOfProductAndOrderCounts);
        when(orderItemService.convertAllProductAndOrderCountToProduct(emptyListOfProductAndOrderCounts))
                .thenReturn(emptyListOfProducts);

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

        when(missionService.getMissionByName(any(String.class))).thenReturn(java.util.Optional.ofNullable(mission));
        when(productService.saveProduct(productService.convertProductDTOToProduct(productDTO))).thenReturn(product1);

        MvcResult mvcResult = mockMvc.perform(post("/api/products")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isOk())
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(product1);
        String actualResponseBody =  mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualTo(expectedResponseBody);
    }

    @Test
    void addProduct_whenAcquisitionDateBeforeMissionStartDate_thenReturns200AndMission() throws Exception {

        productDTO.setAcquisitionDate(mission.getStartDate().minusHours(1L));

        when(missionService.getMissionByName(any(String.class))).thenReturn(java.util.Optional.ofNullable(mission));
        when(productService.saveProduct(productService.convertProductDTOToProduct(productDTO))).thenReturn(product1);

        MvcResult mvcResult = mockMvc.perform(post("/api/products")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isBadRequest())
                .andReturn();

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setMessage("Product AcquisitionDate " + productDTO.getAcquisitionDate()
                + " is before mission start date " + mission.getStartDate());

        String expectedResponseBody = objectMapper.writeValueAsString(errorResponse);
        String actualResponseBody =  mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualTo(expectedResponseBody);
    }

    @Test
    void addProduct_whenAcquisitionDateAfterMissionFinishtDate_thenReturns200AndMission() throws Exception {

        productDTO.setAcquisitionDate(mission.getFinishDate().plusHours(1L));

        when(missionService.getMissionByName(any(String.class))).thenReturn(java.util.Optional.ofNullable(mission));
        when(productService.saveProduct(productService.convertProductDTOToProduct(productDTO))).thenReturn(product1);

        MvcResult mvcResult = mockMvc.perform(post("/api/products")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isBadRequest())
                .andReturn();

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setMessage("Product AcquisitionDate " + productDTO.getAcquisitionDate()
                + " is after mission start date " + mission.getStartDate());

        String expectedResponseBody = objectMapper.writeValueAsString(errorResponse);
        String actualResponseBody =  mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualTo(expectedResponseBody);
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

        assertThat(actualResponseBody).isEqualTo(expectedResponseBody);
    }

    @Test
    void addProduct_whenMissionDoesNotExist_thenReturns404AndErrorResponse() throws Exception {

        when(productService.doesMissionExist(any(String.class))).thenReturn(false);

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

        assertThat(actualResponseBody).isEqualTo(expectedResponseBody);
    }

    //  ----------------------------------------------------------------------------------------------

    @Test
    void deleteProductById_whenValidInput_thenReturns200() throws Exception {

        long productId = 1L;
        when(productService.getProductById(any(Long.class))).thenReturn(product1);
        when(productOrderService.isOrderedProductById(productId)).thenReturn(false);

        mockMvc.perform(delete("/api/products/{id}", productId))
                .andExpect(status().isOk());
    }

    @Test
    void deleteProductById_whenProductDoesNotExist_thenReturns404AndErrorResponse() throws Exception {

        long productId = 1L;
        when(productService.getProductById(any(Long.class))).thenReturn(null);

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

        long productId = 1L;
        when(productService.getProductById(any(Long.class))).thenReturn(product1);
        when(productOrderService.isOrderedProductById(productId)).thenReturn(true);

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