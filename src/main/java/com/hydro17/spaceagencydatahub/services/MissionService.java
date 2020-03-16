package com.hydro17.spaceagencydatahub.services;

import com.hydro17.spaceagencydatahub.models.Mission;
import com.hydro17.spaceagencydatahub.repositories.MissionRepository;
import org.springframework.stereotype.Service;

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

    public boolean isMissionNameUnique(String missionName) {
        if (missionRepository.findByName(missionName).isPresent()) return false;
        return true;
    }

    public Mission saveMission(Mission mission) {
        Mission missionWithSetId = missionRepository.save(mission);
        return missionWithSetId;
    }

    public Mission updateMission(Mission missionChanged) {
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
