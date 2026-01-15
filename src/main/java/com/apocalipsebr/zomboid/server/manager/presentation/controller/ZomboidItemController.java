package com.apocalipsebr.zomboid.server.manager.presentation.controller;

import com.apocalipsebr.zomboid.server.manager.application.service.ZomboidItemService;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.ZomboidItem;
import com.apocalipsebr.zomboid.server.manager.presentation.dto.CategoryBatchDTO;
import com.apocalipsebr.zomboid.server.manager.presentation.dto.ItemDTO;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/zomboid-items")
public class ZomboidItemController {
    
    private static final Logger logger = Logger.getLogger(ZomboidItemController.class.getName());
    
    private final ZomboidItemService zomboidItemService;

    public ZomboidItemController(ZomboidItemService zomboidItemService) {
        this.zomboidItemService = zomboidItemService;
    }

    @PostMapping
    public ResponseEntity<?> createItem(@RequestBody ZomboidItem item) {
        try {
            ZomboidItem created = zomboidItemService.createItem(item);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            logger.severe("Error creating item: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createItemsBatch(@RequestBody List<CategoryBatchDTO> categories) {
        try {
            int created = 0;
            int updated = 0;
            int total = 0;
            
            for (CategoryBatchDTO categoryBatch : categories) {
                String category = categoryBatch.getCategory();
                
                for (ItemDTO itemDTO : categoryBatch.getItems()) {
                    ZomboidItem item = new ZomboidItem();
                    item.setCategory(category);
                    item.setIcon(itemDTO.getIcon());
                    item.setName(itemDTO.getName());
                    item.setPage(itemDTO.getPage());
                    item.setItemId(itemDTO.getItemId());
                    item.setSellable(false); // Default to false
                    
                    boolean exists = zomboidItemService.getItemByItemId(item.getItemId()).isPresent();
                    zomboidItemService.createItem(item);
                    
                    if (exists) {
                        updated++;
                    } else {
                        created++;
                    }
                    total++;
                }
            }
            
            return ResponseEntity.ok(Map.of(
                "message", "Batch operation completed",
                "created", created,
                "updated", updated,
                "total", total
            ));
        } catch (Exception e) {
            logger.severe("Error in batch creation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<ZomboidItem>> getAllItems() {
        List<ZomboidItem> items = zomboidItemService.getAllItems();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getItemById(@PathVariable Long id) {
        return zomboidItemService.getItemById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/item-id/{itemId}")
    public ResponseEntity<?> getItemByItemId(@PathVariable String itemId) {
        return zomboidItemService.getItemByItemId(itemId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/sellable")
    public ResponseEntity<List<ZomboidItem>> getSellableItems() {
        List<ZomboidItem> items = zomboidItemService.getSellableItems();
        return ResponseEntity.ok(items);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ZomboidItem>> searchItems(@RequestParam String query) {
        List<ZomboidItem> items = zomboidItemService.searchItems(query);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<ZomboidItem>> getItemsByCategory(@PathVariable String category) {
        List<ZomboidItem> items = zomboidItemService.getItemsByCategory(category);
        return ResponseEntity.ok(items);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateItem(@PathVariable Long id, @RequestBody ZomboidItem item) {
        try {
            ZomboidItem updated = zomboidItemService.updateItem(id, item);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.severe("Error updating item: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/sellable")
    public ResponseEntity<?> updateSellableStatus(
            @PathVariable Long id, 
            @RequestBody Map<String, Boolean> request) {
        try {
            Boolean sellable = request.get("sellable");
            if (sellable == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "sellable field is required"));
            }
            
            ZomboidItem updated = zomboidItemService.updateSellableStatus(id, sellable);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.severe("Error updating sellable status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(Map.of(
            "totalItems", zomboidItemService.getTotalItemCount(),
            "sellableItems", zomboidItemService.getSellableItemCount()
        ));
    }
}
