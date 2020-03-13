package com.hydro17.spaceagencydatahub.controllers;

import com.hydro17.spaceagencydatahub.exceptions.MissionErrorResponse;
import com.hydro17.spaceagencydatahub.exceptions.MissionNameNotUniqueException;
import com.hydro17.spaceagencydatahub.exceptions.MissionNotFoundException;
import com.hydro17.spaceagencydatahub.models.Mission;
import com.hydro17.spaceagencydatahub.services.MissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/missions")
public class MissionController {

    private MissionService _missionService;
    private Logger logger = LoggerFactory.getLogger(MissionController.class);

    MissionController(MissionService missionService) {
        this._missionService = missionService;
    }

    @GetMapping
    public List<Mission> getAllMissions() {
        return _missionService.getAllMissions();
    }

    @GetMapping("/{id}")
    public Mission getMissionById(@PathVariable long id) {

        Mission mission = _missionService.getMissionById(id);

        if (mission == null) {
            throw new MissionNotFoundException("There is no mission with id: " + id);
        }

        return mission;
    }

    @PostMapping
    public Mission saveMission(@RequestBody Mission mission, BindingResult bindingResult) {

//        if (bindingResult.hasErrors()) {
//            throw new
//        }

        if (_missionService.isMissionNameUnique(mission.getName()) == false) {
            throw new MissionNameNotUniqueException("There is already a mission with the name: " + mission.getName());
        };

        Mission missionWithIdSet = _missionService.saveMission(mission);
        return missionWithIdSet;
    }

    @PutMapping
    public Mission updateMission(@RequestBody Mission mission) {
        return _missionService.updateMission(mission);
    }

    @DeleteMapping("/{id}")
    public Mission deleteMissionById(@PathVariable long id) {
        return _missionService.deleteMissionById(id);
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

//    @ExceptionHandler(NoSuchElementException.class)
//    public ResponseEntity<String> handleException(Exception ex, WebRequest request) {
//        return new ResponseEntity<>("|" + request.toString() + "|", HttpStatus.BAD_REQUEST);
//    }
//
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAllExceptions(Exception ex, WebRequest request) {
        return new ResponseEntity<>("|" + ex.toString() + "|", HttpStatus.BAD_REQUEST);
    }
}
