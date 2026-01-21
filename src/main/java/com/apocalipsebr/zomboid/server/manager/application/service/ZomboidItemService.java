package com.apocalipsebr.zomboid.server.manager.application.service;

import com.apocalipsebr.zomboid.server.manager.domain.entity.app.Character;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.ZomboidItem;
import com.apocalipsebr.zomboid.server.manager.domain.repository.app.ZomboidItemRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Service
public class ZomboidItemService {
    
    private static final Logger logger = Logger.getLogger(ZomboidItemService.class.getName());
    
    private final ZomboidItemRepository zomboidItemRepository;
    private final CharacterService characterService;

    public ZomboidItemService(ZomboidItemRepository zomboidItemRepository,CharacterService characterService) {
        this.zomboidItemRepository = zomboidItemRepository;
        this.characterService = characterService;
    }

    @Transactional
    public ZomboidItem createItem(ZomboidItem item) {
        logger.info("Creating new Zomboid item: " + item.getName());
        
        // Check if itemId already exists
        Optional<ZomboidItem> existing = zomboidItemRepository.findByItemId(item.getItemId());
        if (existing.isPresent()) {
            logger.warning("Item with itemId " + item.getItemId() + " already exists. Updating instead.");
            ZomboidItem existingItem = existing.get();
            existingItem.setName(item.getName());
            existingItem.setCategory(item.getCategory());
            existingItem.setIcon(item.getIcon());
            existingItem.setPage(item.getPage());
            return zomboidItemRepository.save(existingItem);
        }
        
        return zomboidItemRepository.save(item);
    }

    public List<ZomboidItem> getAllItems() {
        return zomboidItemRepository.findAllByOrderByNameAsc();
    }

    public Optional<ZomboidItem> getItemById(Long id) {
        return zomboidItemRepository.findById(id);
    }

    public Optional<ZomboidItem> getItemByItemId(String itemId) {
        return zomboidItemRepository.findByItemId(itemId);
    }

    public List<ZomboidItem> getSellableItems() {
        return zomboidItemRepository.findBySellableTrue();
    }

    public List<ZomboidItem> searchItems(String query) {
        return zomboidItemRepository.findByNameContainingIgnoreCaseOrItemIdContainingIgnoreCase(query, query);
    }

    public List<ZomboidItem> getItemsByCategory(String category) {
        return zomboidItemRepository.findByCategory(category);
    }

    @Transactional
    public ZomboidItem updateItem(Long id, ZomboidItem updatedItem) {
        logger.info("Updating Zomboid item with id: " + id);
        
        ZomboidItem item = zomboidItemRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Item not found with id: " + id));
        
        item.setName(updatedItem.getName());
        item.setCategory(updatedItem.getCategory());
        item.setIcon(updatedItem.getIcon());
        item.setPage(updatedItem.getPage());
        item.setItemId(updatedItem.getItemId());
        item.setSellable(updatedItem.getSellable());
        item.setCustom(updatedItem.getCustom());
        item.setStoreDescription(updatedItem.getStoreDescription());
        item.setValue(updatedItem.getValue());
        
        return zomboidItemRepository.save(item);
    }

    @Transactional
    public ZomboidItem updateSellableStatus(Long id, Boolean sellable) {
        logger.info("Updating sellable status for item id " + id + " to: " + sellable);
        
        ZomboidItem item = zomboidItemRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Item not found with id: " + id));
        
        item.setSellable(sellable);
        return zomboidItemRepository.save(item);
    }

    public long getTotalItemCount() {
        return zomboidItemRepository.count();
    }

    public long getSellableItemCount() {
        return zomboidItemRepository.findBySellableTrue().size();
    }

    // Paginated methods
    public Page<ZomboidItem> getAllItemsPaginated(Pageable pageable) {
        return zomboidItemRepository.findAll(pageable);
    }

    public Page<ZomboidItem> getSellableItemsPaginated(Pageable pageable) {
        return zomboidItemRepository.findBySellable(true, pageable);
    }

    public Page<ZomboidItem> searchItemsPaginated(String query, Pageable pageable) {
        return zomboidItemRepository.findByNameContainingIgnoreCaseOrItemIdContainingIgnoreCaseAndSellableTrue(query, query, pageable);
    }

    public Page<ZomboidItem> getItemsByCategoryPaginated(String category, Pageable pageable) {
        return zomboidItemRepository.findByCategoryContainingIgnoreCaseAndSellableTrue(category, pageable);
    }
    
    public List<String> getAllCategories() {
        return zomboidItemRepository.findDistinctCategories();
    }
    
    public List<String> getSellableCategories() {
        return zomboidItemRepository.findDistinctCategoriesFromSellableItems();
    }

    public record PurchaseResult(boolean success, String message) {}

    @Transactional
    public PurchaseResult purchaseItem(Long itemId, Long characterId, List<Character> userCharacters) {
        // Validate item exists and is sellable
        ZomboidItem item = zomboidItemRepository.findById(itemId)
            .orElseThrow(() -> new IllegalArgumentException("Item not found"));
        
        if (!item.getSellable()) {
            return new PurchaseResult(false, "Item não está disponível para compra");
        }
        
        // Validate character exists and belongs to user
        Character targetCharacter = userCharacters.stream()
            .filter(c -> c.getId().equals(characterId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Character not found or does not belong to user"));
        
        // Check if character is online (lastUpdate within last 61 seconds)
        LocalDateTime cutoffTime = LocalDateTime.now().minusSeconds(61);
        if (targetCharacter.getLastUpdate() == null || targetCharacter.getLastUpdate().isBefore(cutoffTime)) {
            return new PurchaseResult(false, "Personagem deve estar online para receber itens. Última vez visto: " + 
                (targetCharacter.getLastUpdate() != null ? targetCharacter.getLastUpdate().toString() : "nunca"));
        }
        
        // Calculate total currency across all characters
        int totalCurrency = userCharacters.stream()
            .mapToInt(c -> c.getCurrencyPoints() != null ? c.getCurrencyPoints() : 0)
            .sum();
        
        if (totalCurrency < item.getValue()) {
            return new PurchaseResult(false, "Moeda insuficiente. Necessário: " + item.getValue() + 
                " ₳, Disponível: " + totalCurrency + " ₳");
        }
        
        // Deduct currency from characters (starting from first, then next, etc.)
        int remainingCost = item.getValue();
        for (Character character : userCharacters) {
            if (remainingCost <= 0) break;
            
            int currentPoints = character.getCurrencyPoints() != null ? character.getCurrencyPoints() : 0;
            if (currentPoints > 0) {
                int deduction = Math.min(currentPoints, remainingCost);
                character.setCurrencyPoints(currentPoints - deduction);
                remainingCost -= deduction;
                logger.info("Deducted " + deduction + " ₳ from character: " + character.getPlayerName());
            }
        }
        characterService.saveAll(userCharacters);
        logger.info("Successfully purchased item " + item.getName() + " for character " + targetCharacter.getPlayerName());
        return new PurchaseResult(true, "Compra realizada com sucesso! O item será entregue para " + targetCharacter.getPlayerName());
    }
}
