package com.hydro17.spaceagencydatahub;

import com.hydro17.spaceagencydatahub.models.Mission;
import com.hydro17.spaceagencydatahub.models.Product;
import com.hydro17.spaceagencydatahub.models.ProductFootprint;
import com.hydro17.spaceagencydatahub.services.MissionService;
import com.hydro17.spaceagencydatahub.services.ProductService;
import com.hydro17.spaceagencydatahub.utils.ImageryType;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@SpringBootApplication
public class SpaceAgencyDataHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpaceAgencyDataHubApplication.class, args);
    }

    @Bean
    CommandLineRunner runner(MissionService missionService, ProductService productService) {
        return args -> {
            Mission mission = new Mission();
            mission.setName("mission100");
            mission.setImageryType(ImageryType.HYPERSPECTRAL);
            mission.setStartDate(LocalDateTime.now());
            mission.setFinishDate(LocalDateTime.now().plusHours(1));

            Mission mission1 = new Mission();
            mission1.setName("mission200");
            mission1.setImageryType(ImageryType.MULTISPECTRAL);
            mission1.setStartDate(LocalDateTime.now());
            mission1.setFinishDate(LocalDateTime.now().plusDays(2));

            missionService.saveMission(mission);
            missionService.saveMission(mission1);

            Product product = new Product();
            product.setMissionName("mission100");
            product.setAcquisitionDate(LocalDateTime.now());
            product.setPrice(new BigDecimal(100.5));
            product.setUrl("http://com");

            ProductFootprint footprint = new ProductFootprint();
            footprint.setStartCoordinateX(10.55);
            footprint.setStartCoordinateY(20.66);
            footprint.setEndCoordinateX(50.77);
            footprint.setEndCoordinateY(60.88);

            product.setFootprint(footprint);

            productService.saveProduct(product);
        };
    }

}
