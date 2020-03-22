package com.hydro17.spaceagencydatahub.services;

import com.hydro17.spaceagencydatahub.models.Mission;

import java.util.List;
import java.util.Optional;

public interface IMissionService {
    List<Mission> getAllMissions();
    Mission getMissionById(long id);
    Optional<Mission> getMissionByName(String missionName);
    boolean isMissionNameUnique(String missionName);
    Mission saveMission(Mission mission);
    Mission updateMission(Mission missionChanged);
    void deleteMissionById(long id);
}
