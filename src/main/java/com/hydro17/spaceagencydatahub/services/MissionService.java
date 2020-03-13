package com.hydro17.spaceagencydatahub.services;

import com.hydro17.spaceagencydatahub.models.Mission;
import com.hydro17.spaceagencydatahub.repositories.MissionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MissionService {

    private MissionRepository _missionRepository;

    MissionService(MissionRepository missionRepository) {
        this._missionRepository = missionRepository;
    }

    public List<Mission> getAllMissions() {
        return _missionRepository.findAll();
    }

    public Mission getMissionById(long id) {
        Optional<Mission> mission =  _missionRepository.findById(id);
        return mission.get();
    }

    public boolean isMissionNameUnique(String missionName) {
        if (_missionRepository.findByName(missionName).isPresent()) return false;
        return true;
    }

    public Mission saveMission(Mission mission) {
        _missionRepository.save(mission);
        return mission;
    }

    public Mission updateMission(Mission missionChanged) {
        Mission mission = _missionRepository.findById(missionChanged.getId()).get();
        mission.setName(missionChanged.getName());
        mission.setImageryType(missionChanged.getImageryType());
        mission.setStartDate(missionChanged.getStartDate());
        mission.setFinishDate((missionChanged.getFinishDate()));

        return mission;
    }

    public Mission deleteMissionById(long id) {
        Mission mission = _missionRepository.findById(id).get();
        _missionRepository.deleteById(id);
        return mission;
    }
}
