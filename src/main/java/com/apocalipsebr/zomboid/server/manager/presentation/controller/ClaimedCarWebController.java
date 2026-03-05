package com.apocalipsebr.zomboid.server.manager.presentation.controller;

import com.apocalipsebr.zomboid.server.manager.application.service.ClaimedCarService;
import com.apocalipsebr.zomboid.server.manager.application.service.ClaimedCarItemService;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.ClaimedCar;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.User;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/claimed-cars")
public class ClaimedCarWebController {

    private final ClaimedCarService claimedCarService;
    private final ClaimedCarItemService claimedCarItemService;

    public ClaimedCarWebController(ClaimedCarService claimedCarService,
                                    ClaimedCarItemService claimedCarItemService) {
        this.claimedCarService = claimedCarService;
        this.claimedCarItemService = claimedCarItemService;
    }

    // --- Player endpoints (session-based auth) ---

    /**
     * List the logged-in player's claimed cars.
     */
    @GetMapping
    public String myClaimedCars(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<ClaimedCar> cars = claimedCarService.getClaimedCarsByUserId(user.getId());
        model.addAttribute("claimedCars", cars);
        model.addAttribute("user", user);

        return "claimed-cars";
    }

    /**
     * REST API: returns claimed cars for a given user as JSON.
     * Used by the pzmap vehicles overlay module to render car markers on the map.
     */
    @GetMapping("/api/vehicles")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getVehiclesForMap(
            @RequestParam(required = false) String userId,
            HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        boolean isAdmin = "ADMIN".equals(user.getRole());
        List<ClaimedCar> cars;

        // "all" returns every car (admin only)
        if ("all".equalsIgnoreCase(userId) && isAdmin) {
            cars = claimedCarService.getAllClaimedCars();
        } else {
            Long targetUserId;
            try {
                targetUserId = (userId != null) ? Long.parseLong(userId) : user.getId();
            } catch (NumberFormatException e) {
                targetUserId = user.getId();
            }
            if (!isAdmin) {
                targetUserId = user.getId();
            }
            cars = claimedCarService.getClaimedCarsByUserId(targetUserId);
        }
        List<Map<String, Object>> result = new ArrayList<>();

        for (ClaimedCar car : cars) {
            Map<String, Object> carData = new HashMap<>();
            carData.put("id", car.getId());
            carData.put("vehicleHash", car.getVehicleHash());
            carData.put("vehicleName", car.getVehicleName());
            carData.put("scriptName", car.getScriptName());
            carData.put("ownerName", car.getOwnerName());
            carData.put("x", car.getX());
            carData.put("y", car.getY());
            carData.put("preservedForMigration", car.getPreservedForMigration());
            carData.put("itemCount", car.getItems() != null ? car.getItems().size() : 0);
            result.add(carData);
        }

        return ResponseEntity.ok(result);
    }

    // --- Admin endpoints ---

    /**
     * Admin: list all claimed cars across all users.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/manage")
    public String manageClaimedCars(Model model) {
        List<ClaimedCar> allCars = claimedCarService.getAllClaimedCars();
        model.addAttribute("claimedCars", allCars);
        model.addAttribute("totalCars", allCars.size());

        long preservedCount = allCars.stream().filter(ClaimedCar::getPreservedForMigration).count();
        model.addAttribute("preservedCount", preservedCount);
        model.addAttribute("activeCount", allCars.size() - preservedCount);

        return "claimed-cars-manage";
    }

}
