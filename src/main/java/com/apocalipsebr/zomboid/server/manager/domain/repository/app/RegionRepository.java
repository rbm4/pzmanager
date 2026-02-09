package com.apocalipsebr.zomboid.server.manager.domain.repository.app;

import com.apocalipsebr.zomboid.server.manager.domain.entity.app.Region;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {

    List<Region> findAllByOrderByNameAsc();

    List<Region> findByEnabledTrue();

    Optional<Region> findByCode(String code);

    @Query("SELECT r FROM Region r WHERE " +
           "(:search IS NULL OR :search = '' OR LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(r.code) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:enabledOnly = false OR r.enabled = true) AND " +
           "(:category IS NULL OR :category = '' OR LOWER(r.categories) LIKE LOWER(CONCAT('%', :category, '%')))")
    Page<Region> searchRegions(@Param("search") String search,
                               @Param("enabledOnly") Boolean enabledOnly,
                               @Param("category") String category,
                               Pageable pageable);
}
