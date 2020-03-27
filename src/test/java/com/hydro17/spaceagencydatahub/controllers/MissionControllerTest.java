package com.hydro17.spaceagencydatahub.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hydro17.spaceagencydatahub.exceptions.ErrorResponse;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MissionController.class)
@WithMockUser(roles = "CONTENT_MANAGER")
class MissionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MissionService missionService;

//  Added due to CommandLineRunner in the class SpaceAgencyDataHubApplication
    @MockBean
    private ProductService productService;

//  Added due to CommandLineRunner in the class SpaceAgencyDataHubApplication
    @MockBean
    private ProductOrderService productOrderService;

    private List<Mission> emptyListOfMissions;
    private List<Mission> nonEmptyListOfMissions;

    private Mission missionWithOneProduct;
    private Mission missionWithNullImageryTypeField;
    private Mission mission;

    @BeforeEach
    void setUp() {
        emptyListOfMissions = new ArrayList<>();
        nonEmptyListOfMissions = new ArrayList<>();

        missionWithNullImageryTypeField = new Mission();
        missionWithNullImageryTypeField.setId(1L);
        missionWithNullImageryTypeField.setName("mission without imagery type");
        missionWithNullImageryTypeField.setStartDate(LocalDateTime.now());
        missionWithNullImageryTypeField.setFinishDate(LocalDateTime.now().plusHours(1L));

        mission = new Mission();
        mission.setId(1L);
        mission.setName("mission no prodducts, no nulll fields");
        mission.setImageryType(ImageryType.HYPERSPECTRAL);
        mission.setStartDate(LocalDateTime.now());
        mission.setFinishDate(LocalDateTime.now().plusHours(1L));

        ProductFootprint footprint = new ProductFootprint();
        footprint.setStartCoordinateLongitude(10.5);
        footprint.setEndCoordinateLongitude(50.7);
        footprint.setStartCoordinateLatitude(100.15);
        footprint.setEndCoordinateLongitude(200.99);

        Product product = new Product();
        product.setId(1L);
        product.setAcquisitionDate(LocalDateTime.now());
        product.setFootprint(footprint);
        product.setPrice(new BigDecimal("10.5"));
        product.setUrl("http://com");

        missionWithOneProduct = new Mission();
        missionWithOneProduct.setId(2L);
        missionWithOneProduct.setName("mission3");
        missionWithOneProduct.setImageryType(ImageryType.MULTISPECTRAL);
        missionWithOneProduct.setStartDate(LocalDateTime.now());
        missionWithOneProduct.setFinishDate(LocalDateTime.now().plusDays(1L));
        missionWithOneProduct.addProduct(product);
        product.setMission(missionWithOneProduct);

        nonEmptyListOfMissions.add(mission);
        nonEmptyListOfMissions.add(missionWithOneProduct);
    }

//  ----------------------------------------------------------------------------------------------

    @Test
    void getAllMissions_whenValidInput_thenReturns200AndNonEmptyListOfMissions() throws Exception {

        when(missionService.getAllMissions()).thenReturn(nonEmptyListOfMissions);

        MvcResult mvcResult = mockMvc.perform(get("/api/missions")
               .contentType("application/json"))
               .andExpect(status().isOk())
               .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(nonEmptyListOfMissions);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(expectedResponseBody);
    }

    @Test
    void getAllMissions_whenValidInput_thenReturns200AndEmptyListOfMissions() throws Exception {

        when(missionService.getAllMissions()).thenReturn(emptyListOfMissions);

        MvcResult mvcResult = mockMvc.perform(get("/api/missions")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(emptyListOfMissions);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(expectedResponseBody);
    }

    @WithMockUser(roles = "CUSTOMER")
    @Test
    void getAllMissions_whenValidInputAndUserWithWrongRole_thenReturns403() throws Exception {
        mockMvc.perform(get("/api/missions")
                .contentType("application/json"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMissionById_whenValidInput_thenReturns200AndMission() throws Exception {

        when(missionService.getMissionById(any(Long.class))).thenReturn(Optional.ofNullable(mission));

        MvcResult mvcResult = mockMvc.perform(get("/api/missions/{id}", 1L)
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(mission);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(expectedResponseBody);
    }

    @Test
    void getMissionById_whenIdOfNonExistentMission_thenReturns404AndErrorResponse() throws Exception {

        when(missionService.getMissionById(any(Long.class))).thenReturn(Optional.empty());

        MvcResult mvcResult = mockMvc.perform(get("/api/missions/{id}", 1L)
                .contentType("application/json"))
                .andExpect(status().isNotFound())
                .andReturn();

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(HttpStatus.NOT_FOUND.value());
        errorResponse.setMessage("There is no mission with id: " + 1);

        String expectedResponseBody = objectMapper.writeValueAsString(errorResponse);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(expectedResponseBody);
    }

//  -----------------------------------------------------------------------------------------------

    @Test
    void addMission_whenValidInput_thenReturns200AndMission() throws Exception {

        when(missionService.saveMission(any(Mission.class))).thenReturn(mission);
        when(missionService.isMissionNameUnique(any(String.class))).thenReturn(true);

        MvcResult mvcResult = mockMvc.perform(post("/api/missions")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(mission)))
                .andExpect(status().isOk())
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(mission);
        String actualResponseBody =  mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(expectedResponseBody);
    }

    @Test
    void addMission_whenNotUniqueMissionName_thenReturns400AndErrorResponse() throws Exception {

        when(missionService.isMissionNameUnique(any(String.class))).thenReturn(false);

        MvcResult mvcResult = mockMvc.perform(post("/api/missions")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(mission)))
                .andExpect(status().isBadRequest())
                .andReturn();

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setMessage("There is already a mission with the name: " + mission.getName());

        String expectedResponseBody = objectMapper.writeValueAsString(errorResponse);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(expectedResponseBody);
    }

    @Test
    void addMission_whenMissionFieldIsNull_thenReturns400AndErrorResponse() throws Exception {

        assertThat(missionWithNullImageryTypeField.getImageryType()).isNull();

        MvcResult mvcResult = mockMvc.perform(post("/api/missions")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(missionWithNullImageryTypeField)))
                .andExpect(status().isBadRequest())
                .andReturn();

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setMessage("No Mission field can be null");

        String expectedResponseBody = objectMapper.writeValueAsString(errorResponse);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(expectedResponseBody);
    }

//  -----------------------------------------------------------------------------------------------

    @Test
    void updateMission_whenValidInput_thenReturns200AndMission() throws Exception {

        when(missionService.getMissionById(any(Long.class))).thenReturn(Optional.ofNullable(mission));
        when(missionService.updateMission(any(Mission.class))).thenReturn(mission);
        when(missionService.isMissionNameUniqueForMissionsWithOtherIds(any(String.class), any(Long.class))).thenReturn(true);

        MvcResult mvcResult = mockMvc.perform(put("/api/missions")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(mission)))
                .andExpect(status().isOk())
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(mission);
        String actualResponseBody =  mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(expectedResponseBody);
    }

    @Test
    void updateMission_whenIdOfNonExistingMission_thenReturns404AndErrorResponse() throws Exception {

        when(missionService.getMissionById(any(Long.class))).thenReturn(Optional.empty());

        MvcResult mvcResult = mockMvc.perform(put("/api/missions")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(mission)))
                .andExpect(status().isNotFound())
                .andReturn();

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(HttpStatus.NOT_FOUND.value());
        errorResponse.setMessage("There is no mission with id: " + mission.getId());

        String expectedResponseBody = objectMapper.writeValueAsString(errorResponse);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(expectedResponseBody);
    }

    @Test
    void updateMission_whenNotUniqueNewMissionName_thenReturns400AndErrorResponse() throws Exception {

        when(missionService.getMissionById(any(Long.class))).thenReturn(Optional.ofNullable(mission));
        when(missionService.isMissionNameUniqueForMissionsWithOtherIds(any(String.class), any(Long.class))).thenReturn(false);

        MvcResult mvcResult = mockMvc.perform(put("/api/missions")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(mission)))
                .andExpect(status().isBadRequest())
                .andReturn();

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setMessage("There is already a mission with the name: " + mission.getName());

        String expectedResponseBody = objectMapper.writeValueAsString(errorResponse);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(expectedResponseBody);
    }

    @Test
    void updateMission_whenMissionFieldIsNull_thenReturns400AndErrorResponse() throws Exception {

        when(missionService.getMissionById(any(Long.class))).thenReturn(Optional.ofNullable(mission));
        when(missionService.isMissionNameUniqueForMissionsWithOtherIds(any(String.class), any(Long.class))).thenReturn(true);

        assertThat(missionWithNullImageryTypeField.getImageryType()).isNull();

        MvcResult mvcResult = mockMvc.perform(put("/api/missions")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(missionWithNullImageryTypeField)))
                .andExpect(status().isBadRequest())
                .andReturn();

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setMessage("No Mission field can be null");

        String expectedResponseBody = objectMapper.writeValueAsString(errorResponse);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(expectedResponseBody);
    }

//  -----------------------------------------------------------------------------------------------
    @Test
    void deleteMissionById_whenIdOfMissionWithoutProducts_thenReturns200() throws Exception {

        when(missionService.getMissionById(any(Long.class))).thenReturn(Optional.ofNullable(mission));

        mockMvc.perform(delete("/api/missions/{id}", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void deleteMissionById_whenIdOfMissionWithProducts_thenReturns400AndErrorResposne() throws Exception {

        long missionId = 1L;
        when(missionService.getMissionById(any(Long.class))).thenReturn(Optional.ofNullable(missionWithOneProduct));

        MvcResult mvcResult = mockMvc.perform(delete("/api/missions/{id}", missionId))
                .andExpect(status().isBadRequest())
                .andReturn();

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setMessage("Mission: " +  missionWithOneProduct.getName() + " with id: " + missionId
                + " contains " + missionWithOneProduct.getProducts().size()
                + " product(s). Only mission without products can be removed.");

        String expectedResponseBody = objectMapper.writeValueAsString(errorResponse);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(expectedResponseBody);
    }

    @Test
    void deleteMissionById_whenIdOfNonExistingMission_thenReturns404AndErrorResponse() throws Exception {

        long missionId = 1L;
        when(missionService.getMissionById(any(Long.class))).thenReturn(Optional.empty());

        MvcResult mvcResult = mockMvc.perform(delete("/api/missions/{id}", missionId))
                .andExpect(status().isNotFound())
                .andReturn();

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(HttpStatus.NOT_FOUND.value());
        errorResponse.setMessage("There is no mission with id: " + missionId);

        String expectedResponseBody = objectMapper.writeValueAsString(errorResponse);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(expectedResponseBody);
    }
}