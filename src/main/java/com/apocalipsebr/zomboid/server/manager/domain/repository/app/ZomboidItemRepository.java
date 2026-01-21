package com.apocalipsebr.zomboid.server.manager.domain.repository.app;

import com.apocalipsebr.zomboid.server.manager.domain.entity.app.ZomboidItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ZomboidItemRepository extends JpaRepository<ZomboidItem, Long> {
    
    Optional<ZomboidItem> findByItemId(String itemId);
    
    List<ZomboidItem> findAllByOrderByNameAsc();
    
    List<ZomboidItem> findBySellableTrue();
    
    List<ZomboidItem> findByNameContainingIgnoreCaseOrItemIdContainingIgnoreCase(String name, String itemId);
    
    List<ZomboidItem> findByCategory(String category);
    
    @Query("SELECT DISTINCT z.category FROM ZomboidItem z WHERE z.category IS NOT NULL ORDER BY z.category")
    List<String> findDistinctCategories();
    
    @Query("SELECT DISTINCT z.category FROM ZomboidItem z WHERE z.category IS NOT NULL AND z.sellable = true ORDER BY z.category")
    List<String> findDistinctCategoriesFromSellableItems();
    
    // Paginated queries
    Page<ZomboidItem> findBySellable(Boolean sellable, Pageable pageable);
    
    Page<ZomboidItem> findByNameContainingIgnoreCaseOrItemIdContainingIgnoreCaseAndSellableTrue(String name, String itemId, Pageable pageable);
    
    Page<ZomboidItem> findByCategory(String category, Pageable pageable);

    Page<ZomboidItem> findByCategoryContainingIgnoreCaseAndSellableTrue(String category, Pageable pageable);
}
