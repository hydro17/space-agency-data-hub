package com.hydro17.spaceagencydatahub;

import com.hydro17.spaceagencydatahub.models.Mission;
import com.hydro17.spaceagencydatahub.services.MissionService;
import com.hydro17.spaceagencydatahub.utils.ImageryType;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;

@SpringBootApplication
public class SpaceAgencyDataHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpaceAgencyDataHubApplication.class, args);
    }

    @Bean
    CommandLineRunner runner(MissionService missionService) {
        return args -> {
            Mission mission = new Mission();
            mission.setName("mission100");
            mission.setImageryType(ImageryType.HYPERSPECTRAL);
            mission.setFinishDate(LocalDateTime.now());

            Mission mission1 = new Mission();
            mission1.setName("mission200");
            mission1.setImageryType(ImageryType.MULTISPECTRAL);
            mission1.setFinishDate(LocalDateTime.now());

            missionService.saveMission(mission);
            missionService.saveMission(mission1);
        };
    }

}
