package com.hydro17.spaceagencydatahub.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hydro17.spaceagencydatahub.exceptions.ErrorResponse;
import com.hydro17.spaceagencydatahub.models.*;
import com.hydro17.spaceagencydatahub.services.MissionService;
import com.hydro17.spaceagencydatahub.services.OrderItemService;
import com.hydro17.spaceagencydatahub.services.ProductOrderService;
import com.hydro17.spaceagencydatahub.services.ProductService;
import com.hydro17.spaceagencydatahub.utils.ImageryType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    //TODO add unit test conversionService.covert(ProductDto, Product.class)
    //TODO change conversionService to mocked version
    @Autowired
    ConversionService conversionService;

    @MockBean
    private MissionService missionService;

    private List<Product> emptyListOfProducts;
    private List<Product> nonEmptyListOfProducts;
    private List<ProductDTO> nonEmptyListOfProductDTOs;
    private List<IProductAndOrderCount> emptyListOfProductAndOrderCounts;
    private List<IProductAndOrderCount> notEmptyListOfProductAndOrderCounts;

    private Product product1;
    private ProductDTO productDTO;
    private ProductDTO productDTOWithIdNotEqualZero;
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
        product1.setId(2L);
        product1.setAcquisitionDate(LocalDateTime.now());
        product1.setFootprint(footprint);
        product1.setPrice(new BigDecimal("10.5"));
        product1.setUrl("http://com1");
        product1.setMission(mission);
        mission.addProduct(product1);

        emptyListOfProducts = new ArrayList<>();

        nonEmptyListOfProducts = new ArrayList<>();
        nonEmptyListOfProducts.add(product1);

        //TODO ? productDTO -> requestProductDTO
        productDTO = new ProductDTO();
        productDTO.setMissionName(product1.getMission().getName());
        productDTO.setAcquisitionDate(product1.getAcquisitionDate());
        productDTO.setFootprint(product1.getFootprint());
        productDTO.setPrice(product1.getPrice());
        productDTO.setUrl(product1.getUrl());

        //TODO ? productDTOWithIdNotEqualZero -> responseProductDTO
        productDTOWithIdNotEqualZero = new ProductDTO();
        productDTOWithIdNotEqualZero.setId(product1.getId());
        productDTOWithIdNotEqualZero.setMissionName(product1.getMission().getName());
        productDTOWithIdNotEqualZero.setAcquisitionDate(product1.getAcquisitionDate());
        productDTOWithIdNotEqualZero.setFootprint(product1.getFootprint());
        productDTOWithIdNotEqualZero.setPrice(product1.getPrice());
        productDTOWithIdNotEqualZero.setUrl(product1.getUrl());

        nonEmptyListOfProductDTOs = new ArrayList<>();
        nonEmptyListOfProductDTOs.add(productDTOWithIdNotEqualZero);

        emptyListOfProductAndOrderCounts = new ArrayList<>();

        notEmptyListOfProductAndOrderCounts = new ArrayList<>();
        notEmptyListOfProductAndOrderCounts.add(new ProductAndOrderCount(product1, 1L));
    }

    //  ----------------------------------------------------------------------------------------------

    @Test
    void getAllProducts_whenValidInput_thenReturns200AndNonEmptyListOfProducts() throws Exception {

        when(productService.getAllProducts()).thenReturn(nonEmptyListOfProducts);
        //TODO change conversionService to mocked version
//        when(conversionService.convert(any(Product.class), same(ProductDTO.class))).thenReturn(productDTOWithIdNotEqualZero);
//        when(conversionService.convert(any(), eq(ProductDTO.class))).thenReturn(productDTOWithIdNotEqualZero);

        MvcResult mvcResult = mockMvc.perform(get("/api/products")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(nonEmptyListOfProductDTOs);
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

        String expectedResponseBody = objectMapper.writeValueAsString(productDTOWithIdNotEqualZero);
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
    void findProduct_whenValidInput_returns200AndNonEmptyListOfProductDTOs() throws Exception {

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
                eq(afterDate), eq(latitude), eq(longitude), eq(imageryType));

        String expectedResponseBody = objectMapper.writeValueAsString(nonEmptyListOfProductDTOs);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualTo(expectedResponseBody);
    }

    @WithMockUser(roles = "CUSTOMER")
    @Test
    void findProduct_whenLowerCaseImageryType_returns200AndNonEmptyListOfProductDTOs() throws Exception {

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
                .param("imageryType", "hyperspectral"))
                .andExpect(status().isOk())
                .andReturn();

        verify(productService, times(1)).getFilteredProducts(eq(missionName), eq(beforeDate),
                eq(afterDate), eq(latitude), eq(longitude), eq(imageryType));

        String expectedResponseBody = objectMapper.writeValueAsString(nonEmptyListOfProductDTOs);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualTo(expectedResponseBody);
    }

    @WithMockUser(roles = "CUSTOMER")
    @Test
    void findProduct_whenInvalidImageryType_returns400AndErrorResponse() throws Exception {

        String missionName = "mission1";
        LocalDateTime beforeDate = LocalDateTime.now();
        LocalDateTime afterDate = LocalDateTime.now().plusHours(1L);
        Double latitude = 10.3;
        Double longitude = 20.7;
//        ImageryType imageryType = ImageryType.HYPERSPECTRAL;
        String imageryTypeAsString = "hyper";

        MvcResult mvcResult = mockMvc.perform(get("/api/products/find")
                .contentType("application/json")
                .param("missionName", missionName)
                .param("beforeDate", String.valueOf(beforeDate))
                .param("afterDate", String.valueOf(afterDate))
                .param("latitude", String.valueOf(latitude))
                .param("longitude", String.valueOf(longitude))
                .param("imageryType", imageryTypeAsString))
                .andExpect(status().isBadRequest())
                .andReturn();

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setMessage("Imagery type " + imageryTypeAsString + " does not exist");

        String expectedResponseBody = objectMapper.writeValueAsString(errorResponse);
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

    // getProductsGroupedByProductIdOrderedByOrderCountDesc
    @Test
    void getMostOrderedProductsDesc_whenValidInput_returns200AndNotEmptyListOfProductDTOs() throws Exception {

        when(orderItemService.getAllProductAndOrderCountGroupedByProductIdOrderedByOrderCountDesc())
                .thenReturn(notEmptyListOfProductAndOrderCounts);
        when(orderItemService.convertAllProductAndOrderCountToProduct(notEmptyListOfProductAndOrderCounts))
                .thenReturn(nonEmptyListOfProducts);

        MvcResult mvcResult = mockMvc.perform(get("/api/products/most-ordered")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(nonEmptyListOfProductDTOs);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualTo(expectedResponseBody);
    }

    // getProductsGroupedByProductIdOrderedByOrderCountDesc
    @Test
    void getMostOrderedProductsDesc_whenValidInput_returns200AndEmptyListOfProducts() throws Exception {

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
    void addProduct_whenValidInput_thenReturns200AndProductDTO() throws Exception {

        when(missionService.getMissionByName(anyString())).thenReturn(Optional.ofNullable(mission));

        //TODO change conversionService to mocked version
        when(productService.saveProduct(conversionService.convert(productDTO, Product.class))).thenReturn(product1);
//        when(productService.saveProduct(product1)).thenReturn(product1);
//        when(conversionService.convert(product1, ProductDTO.class)).thenReturn(productDTOWithIdNotEqualZero);

        MvcResult mvcResult = mockMvc.perform(post("/api/products")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(productDTO)))
                .andExpect(status().isOk())
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(productDTOWithIdNotEqualZero);
        String actualResponseBody =  mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualTo(expectedResponseBody);
    }

    @Test
    void addProduct_whenAcquisitionDateBeforeMissionStartDate_thenReturns400AndErrorResponse() throws Exception {

        productDTO.setAcquisitionDate(mission.getStartDate().minusHours(1L));

        when(missionService.getMissionByName(anyString())).thenReturn(Optional.ofNullable(mission));

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
    void addProduct_whenAcquisitionDateAfterMissionFinishtDate_thenReturns400AndErrorResponse() throws Exception {

        productDTO.setAcquisitionDate(mission.getFinishDate().plusHours(1L));

        when(missionService.getMissionByName(anyString())).thenReturn(Optional.ofNullable(mission));
        //TODO change conversionService to mocked version
        when(productService.saveProduct(conversionService.convert(productDTO, Product.class))).thenReturn(product1);

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

        when(missionService.getMissionByName(anyString())).thenReturn(Optional.empty());

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