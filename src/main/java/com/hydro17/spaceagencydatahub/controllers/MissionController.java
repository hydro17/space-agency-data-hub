package com.hydro17.spaceagencydatahub.controllers;

import com.hydro17.spaceagencydatahub.exceptions.MissionNameNotUniqueException;
import com.hydro17.spaceagencydatahub.exceptions.MissionNotFoundException;
import com.hydro17.spaceagencydatahub.exceptions.MissionNullFieldException;
import com.hydro17.spaceagencydatahub.exceptions.MissionProductExistsException;
import com.hydro17.spaceagencydatahub.models.Mission;
import com.hydro17.spaceagencydatahub.services.MissionService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/missions")
public class MissionController {

    private MissionService missionService;

    MissionController(MissionService missionService) {
        this.missionService = missionService;
    }

    @GetMapping
    public List<Mission> getAllMissions() {
        return missionService.getAllMissions();
    }

    @GetMapping("/{id}")
    public Mission getMissionById(@PathVariable long id) {
        Optional<Mission> missionOptional = missionService.getMissionById(id);
        return missionOptional.orElseThrow(() -> new MissionNotFoundException("There is no mission with id: " + id));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Mission addMission(@Valid @RequestBody Mission mission, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new MissionNullFieldException(bindingResult);
        }

        if (missionService.isMissionNameUnique(mission.getName()) == false) {
            throw new MissionNameNotUniqueException("There is already a mission with the name: " + mission.getName());
        }

        mission.setId(0);

        Mission missionWithSetId = missionService.saveMission(mission);
        return missionWithSetId;
    }

    @PutMapping
    public Mission updateMission(@Valid @RequestBody Mission mission, BindingResult bindingResult) {

        if (missionService.getMissionById(mission.getId()).isPresent() == false) {
            throw new MissionNotFoundException("There is no mission with id: " + mission.getId());
        }

        if (missionService.isMissionNameUniqueForMissionsWithOtherIds(mission.getName(), mission.getId()) == false) {
            throw new MissionNameNotUniqueException("There is already a mission with the name: " + mission.getName());
        }

        if (bindingResult.hasErrors()) {
            throw new MissionNullFieldException(bindingResult);
        }

        Mission updatedMission = missionService.updateMission(mission);
        return updatedMission;
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deleteMissionById(@PathVariable long id) {
        Mission mission = missionService.getMissionById(id)
                .orElseThrow(() -> new MissionNotFoundException("There is no mission with id: " + id));

        if (mission.getProducts().size() > 0) {
            throw new MissionProductExistsException("Mission: " +  mission.getName() + " with id: " + id
                    + " contains " + mission.getProducts().size()
                    + " product(s). Only mission without products can be removed.");
        }

        missionService.deleteMissionById(id);
    }
}
