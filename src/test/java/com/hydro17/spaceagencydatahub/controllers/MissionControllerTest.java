package com.hydro17.spaceagencydatahub.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hydro17.spaceagencydatahub.models.Mission;
import com.hydro17.spaceagencydatahub.models.Product;
import com.hydro17.spaceagencydatahub.models.ProductFootprint;
import com.hydro17.spaceagencydatahub.services.MissionService;
import com.hydro17.spaceagencydatahub.services.ProductOrderService;
import com.hydro17.spaceagencydatahub.services.ProductService;
import com.hydro17.spaceagencydatahub.utils.ImageryType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MissionController.class)
@WithMockUser(username = "ContentManager", roles = {"CONTENT_MANAGER"})
class MissionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    MissionService missionService;

//  Due to CommandLineRunner in SpaceAgencyDataHubApplication
    @MockBean
    ProductService productService;

//  Due to CommandLineRunner in SpaceAgencyDataHubApplication
    @MockBean
    ProductOrderService productOrderService;

    List<Mission> emptyListOfMissions;
    List<Mission> nonEmptyListOfMissions;

    Mission missionWithIdEqualZero;
    Mission missionWithIdNotEqualZero;
    Mission missionWithoutProducts;
    Mission missionWithOneProduct;
    Mission missionWithNullImageryTypeField;
    Mission missionWithIdEqual1;
    Mission missionWithIdEqual1Changed;

    ProductFootprint footprint1;

    Product product1;

    @BeforeEach
    public void init() {
        emptyListOfMissions = new ArrayList<>();
        nonEmptyListOfMissions = new ArrayList<>();

        missionWithIdEqualZero = new Mission();
        missionWithIdEqualZero.setId(0);
        missionWithIdEqualZero.setName("mission1");
        missionWithIdEqualZero.setImageryType(ImageryType.PANCHROMATIC);
        missionWithIdEqualZero.setStartDate(LocalDateTime.now());
        missionWithIdEqualZero.setFinishDate(LocalDateTime.now().plusHours(1L));

        missionWithIdNotEqualZero = new Mission();
        missionWithIdNotEqualZero.setId(1L);
        missionWithIdNotEqualZero.setName("mission1");
        missionWithIdNotEqualZero.setImageryType(ImageryType.PANCHROMATIC);
        missionWithIdNotEqualZero.setStartDate(LocalDateTime.now());
        missionWithIdNotEqualZero.setFinishDate(LocalDateTime.now().plusHours(1L));

        missionWithoutProducts = new Mission();
        missionWithoutProducts.setId(1L);
        missionWithoutProducts.setName("mission2");
        missionWithoutProducts.setImageryType(ImageryType.HYPERSPECTRAL);
        missionWithoutProducts.setStartDate(LocalDateTime.now());
        missionWithoutProducts.setFinishDate(LocalDateTime.now().plusHours(1L));

        missionWithNullImageryTypeField = new Mission();
        missionWithoutProducts.setId(1L);
        missionWithoutProducts.setName("mission without imagery type");
        missionWithoutProducts.setStartDate(LocalDateTime.now());
        missionWithoutProducts.setFinishDate(LocalDateTime.now().plusHours(1L));

        missionWithIdEqual1 = new Mission();
        missionWithIdEqual1.setId(1);

        missionWithIdEqual1Changed = new Mission();
        missionWithIdEqual1Changed.setId(1);
        missionWithoutProducts.setName("mission with id=1");
        missionWithoutProducts.setImageryType(ImageryType.HYPERSPECTRAL);
        missionWithoutProducts.setStartDate(LocalDateTime.now());
        missionWithoutProducts.setFinishDate(LocalDateTime.now().plusHours(1L));

        footprint1 = new ProductFootprint();
        footprint1.setStartCoordinateLongitude(10.5);
        footprint1.setEndCoordinateLongitude(50.7);
        footprint1.setStartCoordinateLatitude(100.15);
        footprint1.setEndCoordinateLongitude(200.99);

        product1 = new Product();
        product1.setId(1L);
        product1.setAcquisitionDate(LocalDateTime.now());
        product1.setFootprint(footprint1);
        product1.setPrice(new BigDecimal("10.5"));
        product1.setUrl("http://com");

        missionWithOneProduct = new Mission();
        missionWithOneProduct.setId(2L);
        missionWithOneProduct.setName("mission3");
        missionWithOneProduct.setImageryType(ImageryType.MULTISPECTRAL);
        missionWithOneProduct.setStartDate(LocalDateTime.now());
        missionWithOneProduct.setFinishDate(LocalDateTime.now().plusDays(1L));
        missionWithOneProduct.addProduct(product1);
        product1.setMission(missionWithOneProduct);

        nonEmptyListOfMissions.add(missionWithoutProducts);
        nonEmptyListOfMissions.add(missionWithOneProduct);
    }

//  ----------------------------------------------------------------------------------------------

    @Test
    void getAllMissions_whenValidInput_thenReturns200_and_NonEmptyListOfMissions() throws Exception {

        when(missionService.getAllMissions()).thenReturn(nonEmptyListOfMissions);

        MvcResult mvcResult = mockMvc.perform(get("/api/missions")
               .contentType("application/json")
//               below is alternative to use: @WithMockUser(username = "ContentManager", roles = {"CONTENT_MANAGER"})
//               .header("Authorization", "Basic Q29udGVudE1hbmFnZXI6Y29udGVudG1hbmFnZXI=")
        )
               .andExpect(status().isOk())
               .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(nonEmptyListOfMissions);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(expectedResponseBody);
    }

    @WithMockUser(roles = {"USER"})
    @Test
    void getAllMissions_whenValidInput_and_userWithWrongRole_thenReturns403() throws Exception {
        mockMvc.perform(get("/api/missions")
                .contentType("application/json"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllMissions_whenValidInput_thenReturns200_and_EmptyListOfMissions() throws Exception {

        when(missionService.getAllMissions()).thenReturn(emptyListOfMissions);

        MvcResult mvcResult = mockMvc.perform(get("/api/missions")
                        .contentType("application/json")
//               below is alternative to use: @WithMockUser(username = "ContentManager", roles = {"CONTENT_MANAGER"})
//               .header("Authorization", "Basic Q29udGVudE1hbmFnZXI6Y29udGVudG1hbmFnZXI=")
        )
                .andExpect(status().isOk())
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(emptyListOfMissions);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(expectedResponseBody);
    }

    @Test
    void getMissionById_whenValidInput_thenReturns200_and_Mission() throws Exception {

        when(missionService.getMissionById(any(Long.class))).thenReturn(missionWithOneProduct);

        MvcResult mvcResult = mockMvc.perform(get("/api/missions/{id}", 1L)
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(missionWithOneProduct);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(expectedResponseBody);
    }

    @Test
    void getMissionById_whenIdOfNonExistingMission_thenReturns404() throws Exception {

        when(missionService.getMissionById(any(Long.class))).thenReturn(null);

        mockMvc.perform(get("/api/missions/{id}", 1L)
                .contentType("application/json"))
                .andExpect(status().isNotFound());
    }

//  -----------------------------------------------------------------------------------------------

    @Test
    void addMission_whenValidInput_thenReturns200_and_MissionWithNonZeroId() throws Exception {

        when(missionService.saveMission(missionWithIdEqualZero)).thenReturn(missionWithIdNotEqualZero);
        when(missionService.isMissionNameUnique(any(String.class))).thenReturn(true);

        assertThat(missionWithIdEqualZero.getId()).isEqualTo(0);
        assertThat(missionWithIdNotEqualZero.getId()).isNotEqualTo(0);

        MvcResult mvcResult = mockMvc.perform(post("/api/missions")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(missionWithIdEqualZero)))
                .andExpect(status().isOk())
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(missionWithIdNotEqualZero);
        String actualResponseBody =  mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(expectedResponseBody);
    }

    @Test
    void addMission_whenNotUniqueMissionName_thenReturns400() throws Exception {

        when(missionService.isMissionNameUnique(any(String.class))).thenReturn(false);

        assertThat(missionWithIdEqualZero.getId()).isEqualTo(0);

        mockMvc.perform(post("/api/missions")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(missionWithIdEqualZero)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addMission_whenMissionFieldIsNull_thenReturns400() throws Exception {

        assertThat(missionWithNullImageryTypeField.getImageryType()).isNull();

        mockMvc.perform(post("/api/missions")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(missionWithNullImageryTypeField)))
                .andExpect(status().isBadRequest());
    }

//  -----------------------------------------------------------------------------------------------

//    @Test
//    void updateMission_whenValidInput_thenReturns200_and_MissionWithNonZeroId() throws Exception {
//
//        when(missionService.getMissionById(missionWithIdEqual1Changed.getId())).thenReturn(missionWithIdEqual1);
//        when(missionService.updateMission(missionWithIdEqual1Changed)).thenReturn(missionWithIdEqual1);
//        when(missionService.isMissionNameUnique(any(String.class))).thenReturn(true);
//
//        assertThat(missionWithIdNotEqualZero.getId()).isNotEqualTo(0);
//
//        MvcResult mvcResult = mockMvc.perform(put("/api/missions")
//                .contentType("application/json")
//                .content(objectMapper.writeValueAsString(missionWithIdEqual1Changed)))
//                .andExpect(status().isOk())
//                .andReturn();
//
//        String expectedResponseBody = objectMapper.writeValueAsString(missionWithIdNotEqualZero);
//        String actualResponseBody =  mvcResult.getResponse().getContentAsString();
//
//        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(expectedResponseBody);
//    }

//  -----------------------------------------------------------------------------------------------
    @Test
    void deleteMissionById_whenIdOfMissionWithoutProducts_thenReturns200() throws Exception {

        when(missionService.getMissionById(any(Long.class))).thenReturn(missionWithoutProducts);

        mockMvc.perform(delete("/api/missions/{id}", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void deleteMissionById_whenIdOfMissionWithProducts_thenReturns400() throws Exception {

        when(missionService.getMissionById(any(Long.class))).thenReturn(missionWithOneProduct);

        mockMvc.perform(delete("/api/missions/{id}", 1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteMissionById_whenIdOfNonExistingMission_thenReturns404() throws Exception {

        when(missionService.getMissionById(any(Long.class))).thenReturn(null);

        mockMvc.perform(delete("/api/missions/{id}", 1L))
                .andExpect(status().isNotFound());
    }
}