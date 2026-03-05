package com.apocalipsebr.zomboid.server.manager.domain.repository.app;

import com.apocalipsebr.zomboid.server.manager.domain.entity.app.ClaimedCar;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.User;

import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClaimedCarRepository extends JpaRepository<ClaimedCar, Long> {

    Optional<ClaimedCar> findByVehicleHash(String vehicleHash);

    List<ClaimedCar> findByUser(User user);

    List<ClaimedCar> findByUserIdOrderByUpdatedAtDesc(Long userId);

    List<ClaimedCar> findByPreservedForMigrationTrue();

    List<ClaimedCar> findByXNotNullAndYNotNull();

    @Modifying
    @Query("DELETE FROM ClaimedCar c WHERE c.vehicleHash NOT IN :activeHashes AND c.preservedForMigration = false")
    @Transactional
    int deleteStaleNonPreservedCars(@Param("activeHashes") Collection<String> activeHashes);

    @Modifying
    @Query("UPDATE ClaimedCar c SET c.preservedForMigration = true")
    @Transactional
    int markAllPreservedForMigration();

    @Modifying
    @Query("DELETE FROM ClaimedCar c WHERE c.preservedForMigration = true")
    @Transactional
    int deleteAllPreservedCars();
}
