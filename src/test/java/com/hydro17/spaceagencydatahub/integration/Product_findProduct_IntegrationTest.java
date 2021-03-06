package com.hydro17.spaceagencydatahub.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hydro17.spaceagencydatahub.exceptions.ErrorResponse;
import com.hydro17.spaceagencydatahub.models.Mission;
import com.hydro17.spaceagencydatahub.models.Product;
import com.hydro17.spaceagencydatahub.models.ProductDTO;
import com.hydro17.spaceagencydatahub.models.ProductFootprint;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@WithMockUser(roles="CUSTOMER")
public class Product_findProduct_IntegrationTest {

    @Autowired
    MissionRepository missionRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductOrderRepository productOrderRepository;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    private List<Product> emptyListOfProducts;
    private List<Product> nonEmptyListOfProducts;

    private ProductDTO productDTOWithIdNotEqualZero;

    @BeforeEach
    void setUp() {
        productOrderRepository.deleteAll();
        productRepository.deleteAll();
        missionRepository.deleteAll();

        Mission mission = new Mission();
        mission.setName("mission1");
        mission.setImageryType(ImageryType.HYPERSPECTRAL);
        mission.setStartDate(LocalDateTime.now().minusHours(1L));
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

        productDTOWithIdNotEqualZero = new ProductDTO();
        productDTOWithIdNotEqualZero.setId(product.getId());
        productDTOWithIdNotEqualZero.setMissionName(product.getMission().getName());
        productDTOWithIdNotEqualZero.setAcquisitionDate(product.getAcquisitionDate());
        productDTOWithIdNotEqualZero.setFootprint(product.getFootprint());
        productDTOWithIdNotEqualZero.setPrice(product.getPrice());
        productDTOWithIdNotEqualZero.setUrl(product.getUrl());

//        nonEmptyListOfProductDTOs = new ArrayList<>();
//        nonEmptyListOfProductDTOs.add(productDTOWithIdNotEqualZero);

        emptyListOfProducts = new ArrayList<>();

        nonEmptyListOfProducts = new ArrayList<>();
        nonEmptyListOfProducts.add(product);

        missionRepository.save(mission);
        productRepository.save(product);

//      Because product is not ordered, method findProduct will return URL as null
        product.setUrl(null);
        productDTOWithIdNotEqualZero.setUrl(null);
    }

    @Test
    void findProduct_whenNoParams_returns200AndListOfAllProductDTOs() throws Exception {

        MvcResult mvcResult = mockMvc.perform(get("/api/products/find")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        ProductDTO expectedOutput = productDTOWithIdNotEqualZero;
        String actualResponseBody = mvcResult.getResponse().getContentAsString();
        ProductDTO actualOutput = Arrays.asList(objectMapper.readValue(actualResponseBody, ProductDTO[].class)).get(0);

        assertThat(actualOutput).isEqualToIgnoringGivenFields(expectedOutput, "id", "footprint");
        assertThat(actualOutput.getFootprint()).isEqualToIgnoringGivenFields(expectedOutput.getFootprint(), "id");
    }

    @Test
    void findProduct_whenValidMissionName_returns200AndFilteredListOfProductDTOs() throws Exception {

        String missionName = "mission1";

        MvcResult mvcResult = mockMvc.perform(get("/api/products/find")
                .contentType("application/json")
                .param("missionName", missionName))
                .andExpect(status().isOk())
                .andReturn();

        ProductDTO expectedOutput = productDTOWithIdNotEqualZero;
        String actualResponseBody = mvcResult.getResponse().getContentAsString();
        ProductDTO actualOutput = Arrays.asList(objectMapper.readValue(actualResponseBody, ProductDTO[].class)).get(0);

        assertThat(actualOutput).isEqualToIgnoringGivenFields(expectedOutput, "id", "footprint");
        assertThat(actualOutput.getFootprint()).isEqualToIgnoringGivenFields(expectedOutput.getFootprint(), "id");
    }

    @Test
    void findProduct_whenNonExistingMissionName_returns200AndEmptyListOfProductDTOs() throws Exception {

        String missionName = "mission2";

        MvcResult mvcResult = mockMvc.perform(get("/api/products/find")
                .contentType("application/json")
                .param("missionName", missionName))
                .andExpect(status().isOk())
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(new ArrayList<ProductDTO>());
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualTo(expectedResponseBody);
    }

    @Test
    void findProduct_whenValidAfterDate_returns200AndFilteredListOfProductDTOs() throws Exception {

        LocalDateTime afterDate = LocalDateTime.now().minusHours(1L);

        MvcResult mvcResult = mockMvc.perform(get("/api/products/find")
                .contentType("application/json")
                .param("afterDate", String.valueOf(afterDate)))
                .andExpect(status().isOk())
                .andReturn();

        ProductDTO expectedOutput = productDTOWithIdNotEqualZero;
        String actualResponseBody = mvcResult.getResponse().getContentAsString();
        ProductDTO actualOutput = Arrays.asList(objectMapper.readValue(actualResponseBody, ProductDTO[].class)).get(0);

        assertThat(actualOutput).isEqualToIgnoringGivenFields(expectedOutput, "id", "footprint");
        assertThat(actualOutput.getFootprint()).isEqualToIgnoringGivenFields(expectedOutput.getFootprint(), "id");
    }

    @Test
    void findProduct_whenInvalidAfterDate_returns200AndEmptyListOfProductDTOs() throws Exception {

        LocalDateTime afterDate = LocalDateTime.now().plusHours(1L);

        MvcResult mvcResult = mockMvc.perform(get("/api/products/find")
                .contentType("application/json")
                .param("afterDate", String.valueOf(afterDate)))
                .andExpect(status().isOk())
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(new ArrayList<ProductDTO>());
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualTo(expectedResponseBody);
    }

    @Test
    void findProduct_whenValidBeforeDateAndAfterDate_returns200AndFilteredListOfProductDTOs() throws Exception {

        LocalDateTime afterDate = LocalDateTime.now().minusHours(1L);
        LocalDateTime beforeDate = LocalDateTime.now().plusHours(1L);

        MvcResult mvcResult = mockMvc.perform(get("/api/products/find")
                .contentType("application/json")
                .param("afterDate", String.valueOf(afterDate))
                .param("beforeDate", String.valueOf(beforeDate)))
                .andExpect(status().isOk())
                .andReturn();

        ProductDTO expectedOutput = productDTOWithIdNotEqualZero;
        String actualResponseBody = mvcResult.getResponse().getContentAsString();
        ProductDTO actualOutput = Arrays.asList(objectMapper.readValue(actualResponseBody, ProductDTO[].class)).get(0);

        assertThat(actualOutput).isEqualToIgnoringGivenFields(expectedOutput, "id", "footprint");
        assertThat(actualOutput.getFootprint()).isEqualToIgnoringGivenFields(expectedOutput.getFootprint(), "id");
    }

    @Test
    void findProduct_whenValidLatitudeAndLongitude_returns200AndFilteredListOfProductDTOs() throws Exception {

        Double latitude = 150.3;
        Double longitude = 40.7;

        MvcResult mvcResult = mockMvc.perform(get("/api/products/find")
                .contentType("application/json")
                .param("latitude", String.valueOf(latitude))
                .param("longitude", String.valueOf(longitude)))
                .andExpect(status().isOk())
                .andReturn();

        ProductDTO expectedOutput = productDTOWithIdNotEqualZero;
        String actualResponseBody = mvcResult.getResponse().getContentAsString();
        ProductDTO actualOutput = Arrays.asList(objectMapper.readValue(actualResponseBody, ProductDTO[].class)).get(0);

        assertThat(actualOutput).isEqualToIgnoringGivenFields(expectedOutput, "id", "footprint");
        assertThat(actualOutput.getFootprint()).isEqualToIgnoringGivenFields(expectedOutput.getFootprint(), "id");
    }

    @Test
    void findProduct_whenInvalidLatitudeOrLongitude_returns200AndEmptyListOfProductDTOs() throws Exception {

        Double latitude = 150.3;
        Double longitude = 200.7;

        MvcResult mvcResult = mockMvc.perform(get("/api/products/find")
                .contentType("application/json")
                .param("latitude", String.valueOf(latitude))
                .param("longitude", String.valueOf(longitude)))
                .andExpect(status().isOk())
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(new ArrayList<ProductDTO>());
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualTo(expectedResponseBody);
    }

    @Test
    void findProduct_whenValidImageryType_returns200AndFilteredListOfProductDTOs() throws Exception {

        ImageryType imageryType = ImageryType.HYPERSPECTRAL;

        MvcResult mvcResult = mockMvc.perform(get("/api/products/find")
                .contentType("application/json")
                .param("imageryType", String.valueOf(imageryType)))
                .andExpect(status().isOk())
                .andReturn();

        ProductDTO expectedOutput = productDTOWithIdNotEqualZero;
        String actualResponseBody = mvcResult.getResponse().getContentAsString();
        ProductDTO actualOutput = Arrays.asList(objectMapper.readValue(actualResponseBody, ProductDTO[].class)).get(0);

        assertThat(actualOutput).isEqualToIgnoringGivenFields(expectedOutput, "id", "footprint");
        assertThat(actualOutput.getFootprint()).isEqualToIgnoringGivenFields(expectedOutput.getFootprint(), "id");
    }

    @Test
    void findProduct_whenProductsWithImageryTypeDoNotExist_returns200AndEmptyListOfProducts() throws Exception {

        ImageryType imageryType = ImageryType.PANCHROMATIC;

        MvcResult mvcResult = mockMvc.perform(get("/api/products/find")
                .contentType("application/json")
                .param("imageryType", String.valueOf(imageryType)))
                .andExpect(status().isOk())
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(new ArrayList<ProductDTO>());
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualTo(expectedResponseBody);
    }

    @Test
    void findProduct_whenInvalidImageryType_returns400AndErrorResponse() throws Exception {

        String imageryTypeAsString = "HYPER";

        MvcResult mvcResult = mockMvc.perform(get("/api/products/find")
                .contentType("application/json")
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
    void findProduct_whenValidAllParams_returns200AndFilteredListOfProducts() throws Exception {

        String missionName = "mission1";
        LocalDateTime afterDate = LocalDateTime.now().minusHours(1L);
        LocalDateTime beforeDate = LocalDateTime.now().plusHours(1L);
        Double latitude = 150.3;
        Double longitude = 40.7;
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

        ProductDTO expectedOutput = productDTOWithIdNotEqualZero;
        String actualResponseBody = mvcResult.getResponse().getContentAsString();
        ProductDTO actualOutput = Arrays.asList(objectMapper.readValue(actualResponseBody, ProductDTO[].class)).get(0);

        assertThat(actualOutput).isEqualToIgnoringGivenFields(expectedOutput, "id", "footprint");
        assertThat(actualOutput.getFootprint()).isEqualToIgnoringGivenFields(expectedOutput.getFootprint(), "id");
    }
}
