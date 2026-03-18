package com.apocalipsebr.zomboid.server.manager.presentation.controller;

import com.apocalipsebr.zomboid.server.manager.application.service.BackupMigrationService;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.ClaimedCar;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.User;
import com.apocalipsebr.zomboid.server.manager.domain.entity.backup.BackupClaimedCar;
import com.apocalipsebr.zomboid.server.manager.domain.repository.app.UserRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/backup-migration")
@ConditionalOnProperty(name = "backup.datasource.enabled", havingValue = "true", matchIfMissing = false)
public class BackupMigrationController {

    private final BackupMigrationService backupMigrationService;
    private final UserRepository userRepository;

    public BackupMigrationController(BackupMigrationService backupMigrationService,
                                      UserRepository userRepository) {
        this.backupMigrationService = backupMigrationService;
        this.userRepository = userRepository;
    }

    /**
     * Admin page: lists all backup cars tagged for migration, filterable by owner.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public String backupMigrationPage(@RequestParam(required = false) String steamId,
                                       Model model) {
        List<BackupClaimedCar> cars;
        if (steamId != null && !steamId.isBlank()) {
            cars = backupMigrationService.getBackupCarsForMigrationBySteamId(steamId.trim());
            model.addAttribute("filterSteamId", steamId.trim());
        } else {
            cars = backupMigrationService.getBackupCarsForMigration();
        }

        // Group by owner for easier browsing
        Map<String, List<BackupClaimedCar>> carsByOwner = cars.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getOwnerSteamId() != null ? c.getOwnerSteamId() : "unknown",
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        // Collect unique owner Steam IDs for the filter dropdown
        Set<String> ownerSteamIds = cars.stream()
                .map(BackupClaimedCar::getOwnerSteamId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(TreeSet::new));

        model.addAttribute("backupCars", cars);
        model.addAttribute("carsByOwner", carsByOwner);
        model.addAttribute("ownerSteamIds", ownerSteamIds);
        model.addAttribute("totalCars", cars.size());

        return "backup-migration";
    }

    /**
     * Admin action: copies a single backup car into the app datasource.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/copy/{backupCarId}")
    public String copyCarToApp(@PathVariable Long backupCarId,
                                RedirectAttributes redirectAttributes) {
        try {
            BackupClaimedCar backupCar = backupMigrationService.getBackupCarById(backupCarId)
                    .orElseThrow(() -> new IllegalArgumentException("Backup car not found"));

            // Find or resolve target user from the owner's steam ID (±5 tolerance for Lua imprecision)
            long steamIdLong = Long.parseLong(backupCar.getOwnerSteamId());
            User targetUser = userRepository.findByApproximateSteamId(steamIdLong - 12, steamIdLong + 12).orElse(null);

            ClaimedCar copied = backupMigrationService.copyCarToAppDatasource(backupCarId, targetUser);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Veículo '" + copied.getVehicleName() + "' copiado com sucesso para o datasource atual!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Erro ao copiar veículo: " + e.getMessage());
        }
        return "redirect:/backup-migration";
    }

    /**
     * REST API: returns backup car details with items as JSON (for modal detail view).
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/api/car/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getBackupCarDetails(@PathVariable Long id) {
        Optional<BackupClaimedCar> carOpt = backupMigrationService.getBackupCarById(id);
        if (carOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        BackupClaimedCar car = carOpt.get();
        Map<String, Object> result = new HashMap<>();
        result.put("id", car.getId());
        result.put("vehicleHash", car.getVehicleHash());
        result.put("vehicleName", car.getVehicleName());
        result.put("scriptName", car.getScriptName());
        result.put("ownerName", car.getOwnerName());
        result.put("ownerSteamId", car.getOwnerSteamId());
        result.put("x", car.getX());
        result.put("y", car.getY());

        List<Map<String, Object>> items = new ArrayList<>();
        for (var item : car.getItems()) {
            Map<String, Object> itemData = new HashMap<>();
            itemData.put("fullType", item.getFullType());
            itemData.put("count", item.getCount());
            itemData.put("container", item.getContainer());
            items.add(itemData);
        }
        result.put("items", items);

        return ResponseEntity.ok(result);
    }
}
