package com.apocalipsebr.zomboid.server.manager.presentation.controller;

import com.apocalipsebr.zomboid.server.manager.application.service.CharacterService;
import com.apocalipsebr.zomboid.server.manager.application.service.MigrationService;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.Character;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.CharacterMigration;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.ClaimedCar;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.User;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Controller
@RequestMapping("/migration")
public class MigrationController {

    private static final Logger logger = Logger.getLogger(MigrationController.class.getName());

    private final MigrationService migrationService;
    private final CharacterService characterService;

    public MigrationController(MigrationService migrationService, CharacterService characterService) {
        this.migrationService = migrationService;
        this.characterService = characterService;
    }

    /**
     * Migration page — shows current-season characters with migration status
     * and preserved cars available for migration.
     */
    @GetMapping
    public String migrationPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        // Current-season characters
        List<Character> characters = characterService.getUserCharacters(user);

        // Migration records mapped by character id
        Map<Long, CharacterMigration> characterMigrations = migrationService.getUserMigrationsMap(user);

        // Preserved cars for this user
        List<ClaimedCar> preservedCars = migrationService.getPreservedCarsForUser(user);

        // Check if user has any active character with coordinates (needed for car migration)
        boolean hasActiveCharacter = characters.stream()
                .anyMatch(c -> c.getLastX() != null && c.getLastY() != null);

        model.addAttribute("user", user);
        model.addAttribute("characters", characters);
        model.addAttribute("characterMigrations", characterMigrations);
        model.addAttribute("preservedCars", preservedCars);
        model.addAttribute("hasActiveCharacter", hasActiveCharacter);

        return "migration";
    }

    /**
     * Request XP migration for a character.
     */
    @PostMapping("/character/{characterId}")
    public String migrateCharacter(@PathVariable Long characterId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            migrationService.requestCharacterMigration(user, characterId);
            return "redirect:/migration?charSuccess=true";
        } catch (Exception e) {
            logger.warning("Character migration request failed: " + e.getMessage());
            return "redirect:/migration?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
        }
    }

    /**
     * Migrate a single car: write JSONL and delete from database.
     */
    @PostMapping("/car/{carId}")
    public String migrateCar(@PathVariable Long carId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        try {
            migrationService.migrateClaimedCar(user, carId);
            return "redirect:/migration?carSuccess=true";
        } catch (Exception e) {
            logger.warning("Car migration failed: " + e.getMessage());
            return "redirect:/migration?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
        }
    }
}
