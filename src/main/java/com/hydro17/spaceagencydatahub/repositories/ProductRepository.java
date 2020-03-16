package com.hydro17.spaceagencydatahub.repositories;

import com.hydro17.spaceagencydatahub.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    public List<Product> findAllProductsByMissionName(String missionName);
    public List<Product> findAllProductsByMissionNameAndAcquisitionDateBefore(String missionName, LocalDateTime date);
    public List<Product> findAllProductsByMissionNameAndAcquisitionDateAfter(String missionName, LocalDateTime date);



}
