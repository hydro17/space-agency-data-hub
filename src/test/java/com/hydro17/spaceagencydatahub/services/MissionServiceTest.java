package com.hydro17.spaceagencydatahub.services;

import com.hydro17.spaceagencydatahub.models.Mission;
import com.hydro17.spaceagencydatahub.models.Product;
import com.hydro17.spaceagencydatahub.models.ProductFootprint;
import com.hydro17.spaceagencydatahub.repositories.MissionRepository;
import com.hydro17.spaceagencydatahub.utils.ImageryType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = MissionService.class)
class MissionServiceTest {

    @Autowired
    private MissionService missionService;

    @MockBean
    private MissionRepository missionRepository;

    private List<Mission> emptyListOfMissions;
    private List<Mission> nonEmptyListOfMissions;

    private Mission mission;

    @BeforeEach
    void setUp() {
        emptyListOfMissions = new ArrayList<>();

        mission = new Mission();
        mission.setId(1L);
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
        product.setId(1L);
        product.setAcquisitionDate(LocalDateTime.now());
        product.setFootprint(footprint);
        product.setPrice(new BigDecimal("10.5"));
        product.setUrl("http://com");

        Mission missionWithOneProduct = new Mission();
        missionWithOneProduct.setId(2L);
        missionWithOneProduct.setName("mission3");
        missionWithOneProduct.setImageryType(ImageryType.MULTISPECTRAL);
        missionWithOneProduct.setStartDate(LocalDateTime.now());
        missionWithOneProduct.setFinishDate(LocalDateTime.now().plusDays(1L));
        missionWithOneProduct.addProduct(product);
        product.setMission(missionWithOneProduct);

        nonEmptyListOfMissions = new ArrayList<>();
        nonEmptyListOfMissions.add(mission);
        nonEmptyListOfMissions.add(missionWithOneProduct);
    }

    // -------------------------------------------------------------------------------

    @Test
    void getAllMissions_whenValidInput_thenReturnsNonEmptyListOfMissions() {
        when(missionRepository.findAll()).thenReturn(nonEmptyListOfMissions);

        List<Mission> actualOutput = missionService.getAllMissions();

        assertThat(actualOutput).isEqualTo(nonEmptyListOfMissions);
    }

    @Test
    void getAllMissions_whenValidInput_thenReturnsEmptyListOfMissions() {
        when(missionRepository.findAll()).thenReturn(emptyListOfMissions);

        List<Mission> actualOutput = missionService.getAllMissions();

        assertThat(actualOutput).isEqualTo(emptyListOfMissions);
    }

    @Test
    void getMissionById_whenValidInput_thenReturnsMissionOptional() {
        Optional<Mission> missionOptional = Optional.ofNullable(mission);

        when(missionRepository.findById(anyLong())).thenReturn(missionOptional);

        Optional<Mission> actualOutput = missionService.getMissionById(anyLong());

        assertThat(actualOutput).isEqualTo(missionOptional);
    }

    @Test
    void getMissionById_whenIdOfNonExistentMission_thenReturnsEmptyOptional() {
        when(missionRepository.findById(anyLong())).thenReturn(Optional.empty());

        Optional<Mission> actualOutput = missionService.getMissionById(anyLong());

        assertThat(actualOutput).isEqualTo(Optional.empty());
    }

    @Test
    void getMissionByName_whenValidInput_thenReturnsMissionOptional() {
        Optional<Mission> missionOptional = Optional.ofNullable(mission);

        when(missionRepository.findByName(anyString())).thenReturn(missionOptional);

        Optional<Mission> actualOutput = missionService.getMissionByName(anyString());

        assertThat(actualOutput).isEqualTo(missionOptional);
    }

    @Test
    void getMissionByName_whenIdOfNonExistentMission_thenReturnsEmptyOptional() {
        when(missionRepository.findByName(any(String.class))).thenReturn(Optional.empty());

        Optional<Mission> actualOutput = missionService.getMissionByName(anyString());

        assertThat(actualOutput).isEqualTo(Optional.empty());
    }

    // -------------------------------------------------------------------------------

    @Test
    void isMissionNameUnique_whenMissionNameUnique_thenReturnsTrue() {
        when(missionService.getMissionByName(anyString())).thenReturn(Optional.empty());

        Boolean actualOutput = missionService.isMissionNameUnique(anyString());

        assertThat(actualOutput).isTrue();
    }

    @Test
    void isMissionNameUnique_whenMissionNameNotUnique_thenReturnsFalse() {
        when(missionService.getMissionByName(anyString())).thenReturn(Optional.ofNullable(mission));

        Boolean actualOutput = missionService.isMissionNameUnique(anyString());

        assertThat(actualOutput).isFalse();
    }

    @Test
    void isMissionNameUniqueForMissionsWithOtherIds_whenMissionNameUnique_thenReturnsTrue() {
        when(missionRepository.findByNameAndNotEqualId(anyString(), anyLong())).thenReturn(Optional.empty());

        Boolean actualOutput = missionService.isMissionNameUniqueForMissionsWithOtherIds(anyString(), anyLong());

        assertThat(actualOutput).isTrue();
    }

    @Test
    void isMissionNameUniqueForMissionsWithOtherIds_whenMissionNameNotUnique_thenReturnsFalse() {
        when(missionRepository.findByNameAndNotEqualId(anyString(), anyLong())).thenReturn(Optional.ofNullable(mission));

        Boolean actualOutput = missionService.isMissionNameUniqueForMissionsWithOtherIds(anyString(), anyLong());

        assertThat(actualOutput).isFalse();
    }

    // -------------------------------------------------------------------------------

    @Test
    void saveMission_whenMission_thenReturnsMission() {
        when(missionRepository.save(any(Mission.class))).thenReturn(mission);

        Mission actualOutput = missionService.saveMission(mission);

        assertThat(actualOutput).isEqualTo(mission);
    }

    // -------------------------------------------------------------------------------

    @Test
    void updateMission_whenChangedMission_thenReturnsUpdatedMission() {
        Mission changedMission = new Mission();
        changedMission.setId(mission.getId());
        changedMission.setName("changed mission");
        changedMission.setStartDate(LocalDateTime.now());
        changedMission.setFinishDate(LocalDateTime.now().plusHours(2L));
        changedMission.setImageryType(ImageryType.PANCHROMATIC);

        when(missionRepository.findById(anyLong())).thenReturn(Optional.ofNullable(mission));

        Mission actualOutput = missionService.updateMission(changedMission);

        assertThat(actualOutput).isEqualToComparingFieldByField(changedMission);
    }

    // -------------------------------------------------------------------------------

    @Test
    void deleteMissionById_whenInputId_thenReturnsVoid() {
        long missionId = 1L;

        missionService.deleteMissionById(missionId);

        verify(missionRepository, times(1)).deleteById(eq(missionId));
    }
}