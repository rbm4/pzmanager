package com.apocalipsebr.zomboid.server.manager.presentation.controller;

import com.apocalipsebr.zomboid.server.manager.application.service.CharacterService;
import com.apocalipsebr.zomboid.server.manager.application.service.ServerCommandService;
import com.apocalipsebr.zomboid.server.manager.application.service.TransactionLogService;
import com.apocalipsebr.zomboid.server.manager.application.service.ZomboidItemService;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.Character;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.User;
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
import java.util.Map;

@Controller
@RequestMapping("/items")
public class ZomboidItemWebController {

    private final ZomboidItemService zomboidItemService;
    private final CharacterService characterService;
    private final ServerCommandService serverCommandService;
    private final TransactionLogService transactionLogService;

    public ZomboidItemWebController(ZomboidItemService zomboidItemService, CharacterService characterService,
            ServerCommandService serverCommandService, TransactionLogService transactionLogService) {
        this.zomboidItemService = zomboidItemService;
        this.characterService = characterService;
        this.serverCommandService = serverCommandService;
        this.transactionLogService = transactionLogService;
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

        itemsPage = zomboidItemService.getAllItemsPaginated(search, category, pageable);

        model.addAttribute("itemsPage", itemsPage);
        model.addAttribute("items", itemsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", itemsPage.getTotalPages());
        model.addAttribute("totalItems", zomboidItemService.getTotalItemCount());
        model.addAttribute("sellableCount", zomboidItemService.getSellableItemCount());
        model.addAttribute("categories", zomboidItemService.getAllCategories());

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
            itemsPage = zomboidItemService.getAllItemsPaginated(search, null, pageable);
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
            HttpSession session,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("value").descending());
        Page<ZomboidItem> itemsPage;

        if (search != null && !search.trim().isEmpty()) {
            itemsPage = zomboidItemService.searchItemsPaginated(search, pageable);
            model.addAttribute("search", search);
        } else if (category != null && !category.trim().isEmpty()) {
            itemsPage = zomboidItemService.getItemsByCategoryPaginated(category, pageable);
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
        model.addAttribute("categories", zomboidItemService.getSellableCategories());

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

        return "items-store";
    }

    @PostMapping("/purchase")
    @ResponseBody
    public Map<String, Object> purchaseItem(
            @RequestParam Long itemId,
            @RequestParam Long characterId,
            HttpSession session) {

        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Map.of("success", false, "message", "You must be logged in to make purchases");
        }

        List<Character> userCharacters = characterService.getUserCharacters(user);
        if (userCharacters.isEmpty()) {
            return Map.of("success", false, "message", "No characters found for your account");
        }

        ZomboidItemService.PurchaseResult result = zomboidItemService.purchaseItem(itemId, characterId, userCharacters);

        if (result.success()) {
            // Get the target character and item for delivery command
            Character targetCharacter = userCharacters.stream()
                    .filter(c -> c.getId().equals(characterId))
                    .findFirst()
                    .orElse(null);

            ZomboidItem item = zomboidItemService.getItemById(itemId).orElse(null);

            if (targetCharacter != null && item != null) {
                // Execute server command server-side (security)
                String deliveryCommand = "additem \"" + targetCharacter.getPlayerName() + "\" \"" +
                        item.getItemId() + "\" 1";

                try {
                    serverCommandService.sendCommand(deliveryCommand);
                } catch (Exception e) {
                    return Map.of(
                            "success", false,
                            "message",
                            "Compra realizada com sucesso mas falhou ao entregar o item. Contate um administrador.");
                }

                // Calculate new total currency
                int newTotalCurrency = userCharacters.stream()
                        .mapToInt(c -> c.getCurrencyPoints() != null ? c.getCurrencyPoints() : 0)
                        .sum();

                // Log the transaction
                try {
                    transactionLogService.logTransaction(
                        user, targetCharacter, "ITEM_PURCHASE",
                        item.getName(), item.getItemId(),
                        item.getValue(), newTotalCurrency);
                } catch (Exception logEx) {
                    // Don't fail the purchase if logging fails
                }

                return Map.of(
                        "success", true,
                        "message", result.message(),
                        "newTotalCurrency", newTotalCurrency);
            }
        }

        return Map.of("success", result.success(), "message", result.message());
    }

    @GetMapping("/characters/status")
    @ResponseBody
    public Map<String, Object> getCharacterStatuses(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Map.of("success", false, "message", "Not logged in");
        }

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

        return Map.of(
                "success", true,
                "characters", characterStatuses,
                "totalCurrency", totalCurrency);
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

        itemsPage = zomboidItemService.getAllItemsPaginated(search, category, pageable);

        model.addAttribute("itemsPage", itemsPage);
        model.addAttribute("items", itemsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", itemsPage.getTotalPages());
        model.addAttribute("totalItems", zomboidItemService.getTotalItemCount());
        model.addAttribute("sellableCount", zomboidItemService.getSellableItemCount());
        model.addAttribute("categories", zomboidItemService.getSellableCategories());

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
