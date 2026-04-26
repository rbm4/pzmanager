package com.apocalipsebr.zomboid.server.manager.application.service;

import com.apocalipsebr.zomboid.server.manager.domain.entity.app.ClaimedCar;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.ClaimedCarItem;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.User;
import com.apocalipsebr.zomboid.server.manager.domain.entity.backup.BackupClaimedCar;
import com.apocalipsebr.zomboid.server.manager.domain.entity.backup.BackupClaimedCarItem;
import com.apocalipsebr.zomboid.server.manager.domain.repository.app.ClaimedCarRepository;
import com.apocalipsebr.zomboid.server.manager.domain.repository.app.UserRepository;
import com.apocalipsebr.zomboid.server.manager.domain.repository.backup.BackupClaimedCarRepository;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Service
@ConditionalOnProperty(name = "backup.datasource.enabled", havingValue = "true", matchIfMissing = false)
public class BackupMigrationService {

    private static final Logger logger = Logger.getLogger(BackupMigrationService.class.getName());

    private final BackupClaimedCarRepository backupClaimedCarRepository;
    private final ClaimedCarRepository claimedCarRepository;

    public BackupMigrationService(BackupClaimedCarRepository backupClaimedCarRepository,
                                   ClaimedCarRepository claimedCarRepository,
                                   UserRepository userRepository) {
        this.backupClaimedCarRepository = backupClaimedCarRepository;
        this.claimedCarRepository = claimedCarRepository;
    }

    /**
     * Returns all cars from backup that are preserved for migration.
     */
    public List<BackupClaimedCar> getBackupCarsForMigration() {
        return backupClaimedCarRepository.findByPreservedForMigrationTrue();
    }

    /**
     * Returns backup cars preserved for migration filtered by owner Steam ID.
     */
    public List<BackupClaimedCar> getBackupCarsForMigrationBySteamId(String steamId) {
        return backupClaimedCarRepository.findByPreservedForMigrationTrueAndOwnerSteamId(steamId);
    }

    /**
     * Returns all backup cars (regardless of migration flag).
     */
    public List<BackupClaimedCar> getAllBackupCars() {
        return backupClaimedCarRepository.findAll();
    }

    /**
     * Returns a single backup car by ID.
     */
    public Optional<BackupClaimedCar> getBackupCarById(Long id) {
        return backupClaimedCarRepository.findById(id);
    }

    /**
     * Copies a car from the backup datasource into the app datasource.
     * Creates a new ClaimedCar with the same data and items, linked to the given user.
     * The car is marked as preserved for migration in the app datasource.
     */
    @Transactional("appTransactionManager")
    public ClaimedCar copyCarToAppDatasource(Long backupCarId, User targetUser) {
        BackupClaimedCar backupCar = backupClaimedCarRepository.findById(backupCarId)
                .orElseThrow(() -> new IllegalArgumentException("Backup car not found with ID: " + backupCarId));

        ClaimedCar newCar = new ClaimedCar();
        var salt = new java.util.Random().nextInt(9999);
        newCar.setVehicleHash(backupCar.getVehicleHash()+salt);
        newCar.setOwnerSteamId(backupCar.getOwnerSteamId());
        newCar.setOwnerName(backupCar.getOwnerName());
        newCar.setVehicleName(backupCar.getVehicleName());
        newCar.setScriptName(backupCar.getScriptName());
        newCar.setX(backupCar.getX());
        newCar.setY(backupCar.getY());
        newCar.setLastUpdated(backupCar.getLastUpdated());
        newCar.setPreservedForMigration(true);
        newCar.setCreatedAt(LocalDateTime.now());
        newCar.setUpdatedAt(LocalDateTime.now());
        newCar.setUser(targetUser);

        // Copy all items
        for (BackupClaimedCarItem backupItem : backupCar.getItems()) {
            ClaimedCarItem newItem = new ClaimedCarItem(
                    backupItem.getFullType(),
                    backupItem.getCount(),
                    backupItem.getContainer()
            );
            newCar.addItem(newItem);
        }

        ClaimedCar saved = claimedCarRepository.save(newCar);
        logger.info("Copied backup car '" + backupCar.getVehicleName() + "' (hash: " + backupCar.getVehicleHash()
                + ") to app datasource for user " + targetUser.getUsername());
        return saved;
    }
}
