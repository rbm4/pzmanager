package com.apocalipsebr.zomboid.server.manager.domain.repository.app;

import com.apocalipsebr.zomboid.server.manager.domain.entity.app.ZomboidItem;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
