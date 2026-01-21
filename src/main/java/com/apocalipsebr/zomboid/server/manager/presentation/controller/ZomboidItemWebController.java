package com.apocalipsebr.zomboid.server.manager.presentation.controller;

import com.apocalipsebr.zomboid.server.manager.application.service.ZomboidItemService;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.ZomboidItem;

import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/items")
public class ZomboidItemWebController {

    private final ZomboidItemService zomboidItemService;

    public ZomboidItemWebController(ZomboidItemService zomboidItemService) {
        this.zomboidItemService = zomboidItemService;
    }

    @GetMapping
    public String listItems(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean sellable,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<ZomboidItem> itemsPage;

        if (search != null && !search.trim().isEmpty()) {
            itemsPage = zomboidItemService.searchItemsPaginated(search, pageable);
            model.addAttribute("search", search);
        } else if (category != null && !category.trim().isEmpty()) {
            itemsPage = zomboidItemService.getItemsByCategoryPaginated(category, pageable);
            model.addAttribute("category", category);
        } else if (Boolean.TRUE.equals(sellable)) {
            itemsPage = zomboidItemService.getSellableItemsPaginated(pageable);
            model.addAttribute("sellable", true);
        } else {
            itemsPage = zomboidItemService.getAllItemsPaginated(pageable);
        }

        model.addAttribute("itemsPage", itemsPage);
        model.addAttribute("items", itemsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", itemsPage.getTotalPages());
        model.addAttribute("totalItems", zomboidItemService.getTotalItemCount());
        model.addAttribute("sellableCount", zomboidItemService.getSellableItemCount());

        return "items-list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        return "item-create";
    }

    @PostMapping("/create")
    public String createItem(@ModelAttribute ZomboidItem item, RedirectAttributes redirectAttributes) {
        try {
            // Save the item
            zomboidItemService.createItem(item);
            redirectAttributes.addAttribute("success", "created");
            return "redirect:/items/manage";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create item");
            return "redirect:/items/create";
        }
    }

    @GetMapping("/{id}")
    public String itemDetail(@PathVariable Long id, Model model) {
        return zomboidItemService.getItemById(id)
                .map(item -> {
                    model.addAttribute("item", item);
                    return "item-detail";
                })
                .orElse("redirect:/items?error=notfound");
    }

    @GetMapping("/wiki")
    public String wikiView(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "24") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<ZomboidItem> itemsPage;

        if (search != null && !search.trim().isEmpty()) {
            itemsPage = zomboidItemService.searchItemsPaginated(search, pageable);
            model.addAttribute("search", search);
        } else {
            itemsPage = zomboidItemService.getAllItemsPaginated(pageable);
        }

        model.addAttribute("itemsPage", itemsPage);
        model.addAttribute("items", itemsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", itemsPage.getTotalPages());
        model.addAttribute("totalItems", itemsPage.getTotalElements());
        model.addAttribute("wikiMode", true);

        return "items-wiki";
    }

    @GetMapping("/store")
    public String storeView(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "24") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<ZomboidItem> itemsPage;

        if (search != null && !search.trim().isEmpty()) {
            itemsPage = zomboidItemService.searchItemsPaginated(search, pageable);
            itemsPage = new org.springframework.data.domain.PageImpl<>(
                    itemsPage.getContent().stream()
                            .filter(ZomboidItem::getSellable)
                            .toList(),
                    pageable,
                    itemsPage.getTotalElements());
            model.addAttribute("search", search);
        } else if (category != null && !category.trim().isEmpty()) {
            itemsPage = zomboidItemService.getItemsByCategoryPaginated(category, pageable);
            itemsPage = new org.springframework.data.domain.PageImpl<>(
                    itemsPage.getContent().stream()
                            .filter(ZomboidItem::getSellable)
                            .toList(),
                    pageable,
                    itemsPage.getTotalElements());
            model.addAttribute("category", category);
        } else {
            itemsPage = zomboidItemService.getSellableItemsPaginated(pageable);
        }

        model.addAttribute("itemsPage", itemsPage);
        model.addAttribute("items", itemsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", itemsPage.getTotalPages());
        model.addAttribute("totalItems", itemsPage.getTotalElements());
        model.addAttribute("storeMode", true);

        return "items-store";
    }

    @GetMapping("/manage")
    @PreAuthorize("hasRole('ADMIN')")
    public String manageItems(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpSession session,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<ZomboidItem> itemsPage;

        if (search != null && !search.trim().isEmpty()) {
            itemsPage = zomboidItemService.searchItemsPaginated(search, pageable);
            model.addAttribute("search", search);
        } else if (category != null && !category.trim().isEmpty()) {
            itemsPage = zomboidItemService.getItemsByCategoryPaginated(category, pageable);
            model.addAttribute("category", category);
        } else {
            itemsPage = zomboidItemService.getAllItemsPaginated(pageable);
        }

        model.addAttribute("itemsPage", itemsPage);
        model.addAttribute("items", itemsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", itemsPage.getTotalPages());
        model.addAttribute("totalItems", zomboidItemService.getTotalItemCount());
        model.addAttribute("sellableCount", zomboidItemService.getSellableItemCount());

        return "items-manage";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String editItem(@PathVariable Long id, HttpSession session, Model model) {

        return zomboidItemService.getItemById(id)
                .map(item -> {
                    model.addAttribute("item", item);
                    return "item-edit";
                })
                .orElse("redirect:/items/manage?error=notfound");
    }

    @PostMapping("/{id}/edit")
    public String updateItem(
            @PathVariable Long id,
            @ModelAttribute ZomboidItem item,
            HttpSession session) {

        try {
            zomboidItemService.updateItem(id, item);
            return "redirect:/items/manage?success=updated";
        } catch (Exception e) {
            return "redirect:/items/" + id + "/edit?error=failed";
        }
    }

    @PostMapping("/{id}/toggle-sellable")
    public String toggleSellable(
            @PathVariable Long id,
            HttpSession session) {

        try {
            ZomboidItem item = zomboidItemService.getItemById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Item not found"));

            zomboidItemService.updateSellableStatus(id, !item.getSellable());

            return "redirect:/items/manage?success=toggled";
        } catch (Exception e) {
            return "redirect:/items/manage?error=failed";
        }
    }
}
