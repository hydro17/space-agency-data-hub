package com.hydro17.spaceagencydatahub.controllers;

import com.hydro17.spaceagencydatahub.exceptions.MissionErrorResponse;
import com.hydro17.spaceagencydatahub.exceptions.MissionNameNotUniqueException;
import com.hydro17.spaceagencydatahub.exceptions.MissionNotFoundException;
import com.hydro17.spaceagencydatahub.exceptions.MissionNullFieldException;
import com.hydro17.spaceagencydatahub.models.Mission;
import com.hydro17.spaceagencydatahub.services.MissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/missions")
public class MissionController {

    private MissionService missionService;
    private Logger logger = LoggerFactory.getLogger(MissionController.class);

    MissionController(MissionService missionService) {
        this.missionService = missionService;
    }

    @GetMapping
    public List<Mission> getAllMissions() {
        return missionService.getAllMissions();
    }

    @GetMapping("/{id}")
    public Mission getMissionById(@PathVariable long id) {
        Mission mission = missionService.getMissionById(id);

        if (mission == null) {
            throw new MissionNotFoundException("There is no mission with id: " + id);
        }

        return mission;
    }

    @PostMapping
    public Mission addMission(@Valid @RequestBody Mission mission, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new MissionNullFieldException("One of the fields of the Mission object is null");
        }

        if (missionService.isMissionNameUnique(mission.getName()) == false) {
            throw new MissionNameNotUniqueException("There is already a mission with the name: " + mission.getName());
        }

        Mission missionWithSetId = missionService.saveMission(mission);
        return missionWithSetId;
    }

    @PutMapping
    public Mission updateMission(@Valid @RequestBody Mission mission, BindingResult bindingResult) {

        if (missionService.getMissionById(mission.getId()) == null) {
            throw new MissionNotFoundException("There is no mission with id: " + mission.getId());
        }

        if (bindingResult.hasErrors()) {
            throw new MissionNullFieldException("One of fields of the Mission object is null");
        }

        return missionService.updateMission(mission);
    }

    @DeleteMapping("/{id}")
    public void deleteMissionById(@PathVariable long id) {
        Mission mission = missionService.getMissionById(id);

        if (mission == null) {
            throw new MissionNotFoundException("There is no mission with id: " + id);
        }

        missionService.deleteMissionById(id);
    }

    @ExceptionHandler
    public ResponseEntity<MissionErrorResponse> handleException(MissionNotFoundException ex) {

        MissionErrorResponse error = new MissionErrorResponse();
        error.setStatus(HttpStatus.NOT_FOUND.value());
        error.setMessage(ex.getMessage());

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<MissionErrorResponse> handleException(MissionNameNotUniqueException ex) {

        MissionErrorResponse error = new MissionErrorResponse();
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setMessage(ex.getMessage());

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<MissionErrorResponse> handleException(MissionNullFieldException ex) {

        MissionErrorResponse error = new MissionErrorResponse();
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setMessage(ex.getMessage());

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

//    @ExceptionHandler
//    public void handleExceptions(HttpMessageNotReadableException ex) {
//        throw new MissionNullFieldException("Bad data");
//    }

    @ExceptionHandler
    public ResponseEntity<String> handleAllExceptions(Exception ex, WebRequest request) {
        return new ResponseEntity<>("|" + ex.toString() + "|", HttpStatus.BAD_REQUEST);
    }
}
