package com.apocalipsebr.zomboid.server.manager.presentation.controller;

import com.apocalipsebr.zomboid.server.manager.application.service.CarService;
import com.apocalipsebr.zomboid.server.manager.application.service.CharacterService;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.Car;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.Character;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.User;
import com.apocalipsebr.zomboid.server.manager.domain.repository.app.CharacterRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/garage")
public class CarWebController {

    private final CarService carService;
    private final CharacterRepository characterRepository;
    private final CharacterService characterService;

    public CarWebController(CarService carService, CharacterRepository repo, CharacterService characterService) {
        this.characterRepository = repo;
        this.carService = carService;
        this.characterService = characterService;
    }

    // Garage Store - Public view for buying cars
    @GetMapping
    public String garageStore(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            HttpSession session,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Car> carsPage = carService.getAllCarsPaginated(search, true, pageable);

        model.addAttribute("cars", carsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", carsPage.getTotalPages());
        model.addAttribute("search", search);

        // Add user currency and characters if logged in
        User user = (User) session.getAttribute("user");
        if (user != null) {
            List<Character> userCharacters = characterService.getUserCharacters(user);
            int totalCurrency = userCharacters.stream()
                    .mapToInt(c -> c.getCurrencyPoints() != null ? c.getCurrencyPoints() : 0)
                    .sum();

            model.addAttribute("userCharacters", userCharacters);
            model.addAttribute("totalCurrency", totalCurrency);
        } else {
            model.addAttribute("totalCurrency", 0);
        }

        return "garage";
    }

    // Car detail view
    @GetMapping("/{id}")
    public String carDetail(@PathVariable Long id, Model model) {
        return carService.getCarById(id)
                .map(car -> {
                    model.addAttribute("car", car);
                    return "car-detail";
                })
                .orElse("redirect:/garage?error=notfound");
    }

    // Admin - Manage cars
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/manage")
    public String manageCars(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Car> carsPage = carService.getAllCarsPaginated(search, false, pageable);

        model.addAttribute("cars", carsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", carsPage.getTotalPages());
        model.addAttribute("totalCars", carService.getTotalCarCount());
        model.addAttribute("availableCount", carService.getAvailableCarCount());
        model.addAttribute("search", search);

        return "cars-manage";
    }

    // Admin - Show create form
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("car", new Car());
        return "car-create";
    }

    // Admin - Create car
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public String createCar(@ModelAttribute Car car, RedirectAttributes redirectAttributes) {
        try {
            carService.createCar(car);
            redirectAttributes.addAttribute("success", "created");
            return "redirect:/garage/manage";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create car");
            return "redirect:/garage/create";
        }
    }

    // Admin - Show edit form
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return carService.getCarById(id)
                .map(car -> {
                    model.addAttribute("car", car);
                    return "car-edit";
                })
                .orElseGet(() -> {
                    redirectAttributes.addAttribute("error", "notfound");
                    return "redirect:/garage/manage";
                });
    }

    // Admin - Update car
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/edit")
    public String updateCar(@PathVariable Long id, @ModelAttribute Car car, RedirectAttributes redirectAttributes) {
        try {
            carService.updateCar(id, car);
            redirectAttributes.addAttribute("success", "updated");
            return "redirect:/garage/manage";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update car");
            return "redirect:/garage/" + id + "/edit";
        }
    }

    // Admin - Toggle availability
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/toggle-availability")
    public String toggleAvailability(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            carService.toggleAvailability(id);
            redirectAttributes.addAttribute("success", "toggled");
            return "redirect:/garage/manage";
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "failed");
            return "redirect:/garage/manage";
        }
    }

    // Admin - Delete car
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/delete")
    public String deleteCar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            carService.deleteCar(id);
            redirectAttributes.addAttribute("success", "deleted");
            return "redirect:/garage/manage";
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "failed");
            return "redirect:/garage/manage";
        }
    }

    // GET user's characters for purchase modal - AJAX endpoint
    @GetMapping("/characters/list")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUserCharacters(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User user = (User) session.getAttribute("user");
        try {
            var characters = characterRepository.findByUserOrderByZombieKillsDesc(user);
            response.put("success", true);
            response.put("characters", characters);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to load characters");
            return ResponseEntity.status(500).body(response);
        }
    }

    // GET character online status - AJAX endpoint (single character)
    @GetMapping("/characters/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCharacterStatus(@RequestParam Long characterId) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean isOnline = carService.getCharacterStatus(characterId);
            response.put("success", true);
            response.put("isOnline", isOnline);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Falha ao verificar status do personagem");
            return ResponseEntity.status(500).body(response);
        }
    }

    // GET all characters statuses - AJAX endpoint for polling (like items store)
    @GetMapping("/characters/statuses")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCharacterStatuses(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.put("success", false);
            response.put("message", "Não autenticado");
            return ResponseEntity.status(401).body(response);
        }

        try {
            List<Character> userCharacters = characterService.getUserCharacters(user);
            List<Map<String, Object>> characterStatuses = userCharacters.stream()
                    .map(c -> Map.of(
                            "id", (Object) c.getId(),
                            "playerName", (Object) c.getPlayerName(),
                            "currencyPoints", (Object) (c.getCurrencyPoints() != null ? c.getCurrencyPoints() : 0),
                            "isOnline", (Object) (c.getLastUpdate() != null &&
                                    c.getLastUpdate().isAfter(java.time.LocalDateTime.now().minusSeconds(61)))))
                    .toList();

            int totalCurrency = userCharacters.stream()
                    .mapToInt(c -> c.getCurrencyPoints() != null ? c.getCurrencyPoints() : 0)
                    .sum();

            response.put("success", true);
            response.put("characters", characterStatuses);
            response.put("totalCurrency", totalCurrency);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Falha ao carregar personagens");
            return ResponseEntity.status(500).body(response);
        }
    }

    // POST vehicle purchase - AJAX endpoint
    @PostMapping("/purchase")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> purchaseVehicle(
            @RequestParam Long carId,
            @RequestParam Long characterId) {
        var result = carService.purchaseVehicle(carId, characterId);
        
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(400).body(result);
        }
    }
}
