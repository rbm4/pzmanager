package com.apocalipsebr.zomboid.server.manager.presentation.controller;

import com.apocalipsebr.zomboid.server.manager.application.service.CarService;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.Car;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/garage")
public class CarWebController {

    private final CarService carService;

    public CarWebController(CarService carService) {
        this.carService = carService;
    }

    // Garage Store - Public view for buying cars
    @GetMapping
    public String garageStore(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Car> carsPage = carService.getAllCarsPaginated(search, true, pageable);

        model.addAttribute("cars", carsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", carsPage.getTotalPages());
        model.addAttribute("search", search);

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
}
