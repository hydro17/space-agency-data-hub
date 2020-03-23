package com.hydro17.spaceagencydatahub.repositories;

import com.hydro17.spaceagencydatahub.models.Mission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MissionRepository extends JpaRepository<Mission, Long> {

    Optional<Mission> findByName(String name);

    @Query("SELECT m FROM Mission m WHERE m.name = :name AND m.id <> :id")
    Optional<Mission> findByNameAndNotEqualId(String name, Long id);
}
