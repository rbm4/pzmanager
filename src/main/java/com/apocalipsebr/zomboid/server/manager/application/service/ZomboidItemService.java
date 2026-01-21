package com.apocalipsebr.zomboid.server.manager.application.service;

import com.apocalipsebr.zomboid.server.manager.domain.entity.app.ZomboidItem;
import com.apocalipsebr.zomboid.server.manager.domain.repository.app.ZomboidItemRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Service
public class ZomboidItemService {
    
    private static final Logger logger = Logger.getLogger(ZomboidItemService.class.getName());
    
    private final ZomboidItemRepository zomboidItemRepository;

    public ZomboidItemService(ZomboidItemRepository zomboidItemRepository) {
        this.zomboidItemRepository = zomboidItemRepository;
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
        return zomboidItemRepository.findByNameContainingIgnoreCaseOrItemIdContainingIgnoreCase(query, query, pageable);
    }

    public Page<ZomboidItem> getItemsByCategoryPaginated(String category, Pageable pageable) {
        return zomboidItemRepository.findByCategory(category, pageable);
    }
}
