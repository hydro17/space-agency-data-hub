package com.hydro17.spaceagencydatahub;

import com.hydro17.spaceagencydatahub.models.*;
import com.hydro17.spaceagencydatahub.services.IMissionService;
import com.hydro17.spaceagencydatahub.services.MissionService;
import com.hydro17.spaceagencydatahub.services.ProductOrderService;
import com.hydro17.spaceagencydatahub.services.ProductService;
import com.hydro17.spaceagencydatahub.utils.ImageryType;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@SpringBootApplication
public class SpaceAgencyDataHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpaceAgencyDataHubApplication.class, args);
    }

    @Bean
    CommandLineRunner runner(IMissionService missionService, ProductService productService, ProductOrderService productOrderService) {
        return args -> {
            Mission mission = new Mission();
            mission.setName("mission100");
            mission.setImageryType(ImageryType.HYPERSPECTRAL);
            mission.setStartDate(LocalDateTime.now());
            mission.setFinishDate(LocalDateTime.now().plusHours(1L));

            Mission mission1 = new Mission();
            mission1.setName("mission200");
            mission1.setImageryType(ImageryType.MULTISPECTRAL);
            mission1.setStartDate(LocalDateTime.now());
            mission1.setFinishDate(LocalDateTime.now().plusDays(1L));

            missionService.saveMission(mission);
            missionService.saveMission(mission1);

//          -----------------------------------------

            Product product = new Product();
            product.setMission(mission);
            mission.addProduct(product);
            product.setAcquisitionDate(LocalDateTime.now());
            product.setPrice(new BigDecimal("100.5"));
            product.setUrl("http://com");

            ProductFootprint footprint = new ProductFootprint();
            footprint.setStartCoordinateLatitude(10.55);
            footprint.setStartCoordinateLongitude(20.66);
            footprint.setEndCoordinateLatitude(50.77);
            footprint.setEndCoordinateLongitude(60.88);

            product.setFootprint(footprint);


            Product product1 = new Product();
            product1.setMission(mission);
            mission.addProduct(product1);
            product1.setAcquisitionDate(LocalDateTime.now().plusHours(2));
            product1.setPrice(new BigDecimal("110.51"));
            product1.setUrl("http://com");

            ProductFootprint footprint1 = new ProductFootprint();
            footprint1.setStartCoordinateLatitude(11.55);
            footprint1.setStartCoordinateLongitude(21.66);
            footprint1.setEndCoordinateLatitude(31.77);
            footprint1.setEndCoordinateLongitude(61.88);

            product1.setFootprint(footprint1);


            Product product2 = new Product();
            product2.setMission(mission);
            mission.addProduct(product2);
            product2.setAcquisitionDate(LocalDateTime.now().minusHours(1));
            product2.setPrice(new BigDecimal(110.51));
            product2.setUrl("http://com");

            ProductFootprint footprint2 = new ProductFootprint();
            footprint2.setStartCoordinateLatitude(11.55);
            footprint2.setStartCoordinateLongitude(21.66);
            footprint2.setEndCoordinateLatitude(51.77);
            footprint2.setEndCoordinateLongitude(31.88);

            product2.setFootprint(footprint2);

            productService.saveProduct(product);
            productService.saveProduct(product1);
            productService.saveProduct(product2);

//          -----------------------------------------

            ProductOrder productOrder = new ProductOrder();
            productOrder.setPlacedOn(LocalDateTime.now());
            productOrder.addProduct(product);

            ProductOrder productOrder1 = new ProductOrder();
            productOrder1.setPlacedOn(LocalDateTime.now().plusHours(1L));
            productOrder1.addProduct(product1);
            productOrder1.addProduct(product2);

            productOrderService.saveProductOrder(productOrder);
            productOrderService.saveProductOrder(productOrder1);
        };
    }

}
