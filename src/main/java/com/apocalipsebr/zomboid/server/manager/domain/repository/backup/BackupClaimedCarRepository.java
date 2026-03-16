package com.apocalipsebr.zomboid.server.manager.domain.repository.backup;

import com.apocalipsebr.zomboid.server.manager.domain.entity.backup.BackupClaimedCar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BackupClaimedCarRepository extends JpaRepository<BackupClaimedCar, Long> {

    List<BackupClaimedCar> findByPreservedForMigrationTrue();

    List<BackupClaimedCar> findByOwnerSteamId(String ownerSteamId);

    List<BackupClaimedCar> findByPreservedForMigrationTrueAndOwnerSteamId(String ownerSteamId);

    Optional<BackupClaimedCar> findByVehicleHash(String vehicleHash);
}
