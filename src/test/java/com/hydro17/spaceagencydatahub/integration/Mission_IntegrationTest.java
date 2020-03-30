package com.hydro17.spaceagencydatahub.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hydro17.spaceagencydatahub.exceptions.ErrorResponse;
import com.hydro17.spaceagencydatahub.models.Mission;
import com.hydro17.spaceagencydatahub.models.Product;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
@WithMockUser(roles = "CONTENT_MANAGER")
public class Mission_IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MissionRepository missionRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductOrderRepository productOrderRepository;

    private List<Mission> emptyListOfMissions;
    private List<Mission> nonEmptyListOfMissions;

    private Mission missionWithOneProduct;
    private Mission mission;

    @BeforeEach
    void setUp() {
        productOrderRepository.deleteAll();
        productRepository.deleteAll();
        missionRepository.deleteAll();

        emptyListOfMissions = new ArrayList<>();

        mission = new Mission();
        mission.setName("mission1");
        mission.setImageryType(ImageryType.HYPERSPECTRAL);
        mission.setStartDate(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS));
        mission.setFinishDate(LocalDateTime.now().plusHours(1L).truncatedTo(ChronoUnit.MILLIS));

        ProductFootprint footprint = new ProductFootprint();
        footprint.setStartCoordinateLatitude(100.15);
        footprint.setEndCoordinateLatitude(200.99);
        footprint.setStartCoordinateLongitude(10.5);
        footprint.setEndCoordinateLongitude(50.7);

        Product product = new Product();
        product.setAcquisitionDate(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS));
        product.setFootprint(footprint);
        product.setPrice(new BigDecimal("10.5"));
        product.setUrl("http://com");

        missionWithOneProduct = new Mission();
        missionWithOneProduct.setName("mission2");
        missionWithOneProduct.setImageryType(ImageryType.MULTISPECTRAL);
        missionWithOneProduct.setStartDate(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS));
        missionWithOneProduct.setFinishDate(LocalDateTime.now().plusDays(1L).truncatedTo(ChronoUnit.MILLIS));
        missionWithOneProduct.addProduct(product);
        product.setMission(missionWithOneProduct);

        nonEmptyListOfMissions = new ArrayList<>();
        nonEmptyListOfMissions.add(mission);
        nonEmptyListOfMissions.add(missionWithOneProduct);
    }

    @Test
    void getAllMissions_whenNoMissions_thenReturns200AndEmptyListOfMissions() throws Exception {

        MvcResult mvcResult =  mockMvc.perform(get("/api/missions")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(emptyListOfMissions);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualTo(expectedResponseBody);
    }

    @Test
    void getAllMissions_whenAreMissions_thenReturns200AndNonEmptyListOfMissions() throws Exception {

        missionRepository.save(mission);
        missionRepository.save(missionWithOneProduct);

        MvcResult mvcResult =  mockMvc.perform(get("/api/missions")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(nonEmptyListOfMissions);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualTo(expectedResponseBody);
    }

    @Test
    void getMissionById_whenValidInput_thenReturns200AndMission() throws Exception {

        long missionId = missionRepository.save(mission).getId();

        MvcResult mvcResult = mockMvc.perform(get("/api/missions/{id}", missionId)
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(mission);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualTo(expectedResponseBody);
    }

    @Test
    void getMissionById_whenIdOfNonExistingMission_thenReturns404AndErrorResponse() throws Exception {

        Mission savedMissiom =  missionRepository.save(mission);
        long missionId = savedMissiom.getId();
        missionRepository.delete(savedMissiom);

        MvcResult mvcResult = mockMvc.perform(get("/api/missions/{id}", missionId)
                .contentType("application/json"))
                .andExpect(status().isNotFound())
                .andReturn();

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(HttpStatus.NOT_FOUND.value());
        errorResponse.setMessage("There is no mission with id: " + missionId);

        String expectedResponseBody = objectMapper.writeValueAsString(errorResponse);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualTo(expectedResponseBody);
    }

    //  -----------------------------------------------------------------------------------------------

    @Test
    void addMission_whenValidInput_thenReturns200AndMission() throws Exception {

        MvcResult mvcResult = mockMvc.perform(post("/api/missions")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(mission)))
                .andExpect(status().isOk())
                .andReturn();

        String actualResponseBody =  mvcResult.getResponse().getContentAsString();
        Mission responseAsMission = objectMapper.readValue(actualResponseBody, Mission.class);

        assertThat(responseAsMission.getName()).isEqualTo(mission.getName());
        assertThat(responseAsMission.getImageryType()).isEqualTo(mission.getImageryType());
        assertThat(responseAsMission.getStartDate()).isEqualTo(mission.getStartDate());
        assertThat(responseAsMission.getFinishDate()).isEqualTo(mission.getFinishDate());
        assertThat(responseAsMission.getProducts().size()).isZero();
    }

    @Test
    void addMission_whenNotUniqueMissionName_thenReturns400AndErrorResponse() throws Exception {

        missionRepository.save(mission);

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

        assertThat(actualResponseBody).isEqualTo(expectedResponseBody);
    }

    @Test
    void addMission_whenMissionFieldIsNull_thenReturns400AndErrorResponse() throws Exception {

        mission.setImageryType(null);
        Mission missionWithNullImageryType = mission;

        MvcResult mvcResult = mockMvc.perform(post("/api/missions")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(missionWithNullImageryType)))
                .andExpect(status().isBadRequest())
                .andReturn();

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setMessage("No Mission field can be null");

        String expectedResponseBody = objectMapper.writeValueAsString(errorResponse);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualTo(expectedResponseBody);
    }

    //  -----------------------------------------------------------------------------------------------

    @Test
    void updateMission_whenValidInput_thenReturns200AndMission_and_UpdatedMissionFromDb() throws Exception {

        Mission savedMission = missionRepository.save(mission);

        savedMission.setName("updated mission name");

        MvcResult mvcResult = mockMvc.perform(put("/api/missions")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(savedMission)))
                .andExpect(status().isOk())
                .andReturn();

        String expectedResponseBody = objectMapper.writeValueAsString(savedMission);
        String actualResponseBody =  mvcResult.getResponse().getContentAsString();
        String updatedMissionFromDb = objectMapper.writeValueAsString(missionRepository.findById(savedMission.getId()));

        assertThat(actualResponseBody).isEqualTo(expectedResponseBody);
        assertThat(updatedMissionFromDb).isEqualTo(expectedResponseBody);
    }

    @Test
    void updateMission_whenIdOfNonExistingMission_thenReturns404AndErrorResponse() throws Exception {

        mission.setId(1L);

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

        assertThat(actualResponseBody).isEqualTo(expectedResponseBody);
    }

    @Test
    void updateMission_whenNotUniqueNewMissionName_thenReturns400AndErrorResponse() throws Exception {

        Mission savedMission = missionRepository.save(mission);
        missionRepository.save(missionWithOneProduct);

        savedMission.setName(missionWithOneProduct.getName());

        MvcResult mvcResult = mockMvc.perform(put("/api/missions")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(savedMission)))
                .andExpect(status().isBadRequest())
                .andReturn();

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setMessage("There is already a mission with the name: " + mission.getName());

        String expectedResponseBody = objectMapper.writeValueAsString(errorResponse);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualTo(expectedResponseBody);
    }

    @Test
    void updateMission_whenMissionFieldIsNull_thenReturns400AndErrorResponse() throws Exception {

        Mission savedMission = missionRepository.save(mission);
        savedMission.setImageryType(null);
        Mission missionWithNullImageryType = savedMission;

        MvcResult mvcResult = mockMvc.perform(put("/api/missions")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(missionWithNullImageryType)))
                .andExpect(status().isBadRequest())
                .andReturn();

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setMessage("No Mission field can be null");

        String expectedResponseBody = objectMapper.writeValueAsString(errorResponse);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualTo(expectedResponseBody);
    }

    //  -----------------------------------------------------------------------------------------------
    @Test
    void deleteMissionById_whenIdOfMissionWithoutProducts_thenReturns200() throws Exception {

        Mission savedMission = missionRepository.save(mission);

        mockMvc.perform(delete("/api/missions/{id}", savedMission.getId()))
                .andExpect(status().isOk());
    }

    @Test
    void deleteMissionById_whenIdOfMissionWithProducts_thenReturns400AndErrorResposne() throws Exception {

        Mission savedMissionWithOneProduct = missionRepository.save(missionWithOneProduct);
        long missionId = savedMissionWithOneProduct.getId();

        MvcResult mvcResult = mockMvc.perform(delete("/api/missions/{id}", missionId))
                .andExpect(status().isBadRequest())
                .andReturn();

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        errorResponse.setMessage("Mission: " +  savedMissionWithOneProduct.getName() + " with id: " + missionId
                + " contains " + savedMissionWithOneProduct.getProducts().size()
                + " product(s). Only mission without products can be removed.");

        String expectedResponseBody = objectMapper.writeValueAsString(errorResponse);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualTo(expectedResponseBody);
    }

    @Test
    void deleteMissionById_whenIdOfNonExistingMission_thenReturns404AndErrorResponse() throws Exception {

        long missionId = 1L;

        MvcResult mvcResult = mockMvc.perform(delete("/api/missions/{id}", missionId))
                .andExpect(status().isNotFound())
                .andReturn();

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setStatus(HttpStatus.NOT_FOUND.value());
        errorResponse.setMessage("There is no mission with id: " + missionId);

        String expectedResponseBody = objectMapper.writeValueAsString(errorResponse);
        String actualResponseBody = mvcResult.getResponse().getContentAsString();

        assertThat(actualResponseBody).isEqualTo(expectedResponseBody);
    }
}
