package com.hydro17.spaceagencydatahub.services;

import com.hydro17.spaceagencydatahub.models.Mission;
import com.hydro17.spaceagencydatahub.repositories.MissionRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class MissionService {

    private MissionRepository missionRepository;

    MissionService(MissionRepository missionRepository) {
        this.missionRepository = missionRepository;
    }

    public List<Mission> getAllMissions() {
        return missionRepository.findAll();
    }

    public Mission getMissionById(long id) {
        Optional<Mission> mission =  missionRepository.findById(id);
        return mission.orElse(null);
    }

    public Optional<Mission> getMissionByName(String missionName) {
        return missionRepository.findByName(missionName);
    }

    public boolean isMissionNameUnique(String missionName) {
        if (getMissionByName(missionName).isPresent()) return false;
        return true;
    }

    public boolean isMissionNameUniqueForMissionsWithOtherIds(String missionName, Long missionId) {
        if (missionRepository.findByNameAndNotEqualId(missionName, missionId).isPresent()) return false;
        return true;
    }

    public Mission saveMission(Mission mission) {
        Mission missionWithSetId = missionRepository.save(mission);
        return missionWithSetId;
    }

    @Transactional
    public Mission updateMission(Mission missionChanged) {
//      In controller is checked that the mission with the given id exists so we can use get on this optional
        Mission mission = missionRepository.findById(missionChanged.getId()).get();

        mission.setName(missionChanged.getName());
        mission.setImageryType(missionChanged.getImageryType());
        mission.setStartDate(missionChanged.getStartDate());
        mission.setFinishDate((missionChanged.getFinishDate()));

        return mission;
    }

    public void deleteMissionById(long id) {
        missionRepository.deleteById(id);
    }
}
