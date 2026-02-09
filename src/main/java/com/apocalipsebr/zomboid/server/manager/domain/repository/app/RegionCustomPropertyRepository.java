package com.apocalipsebr.zomboid.server.manager.domain.repository.app;

import com.apocalipsebr.zomboid.server.manager.domain.entity.app.RegionCustomProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegionCustomPropertyRepository extends JpaRepository<RegionCustomProperty, Long> {

    List<RegionCustomProperty> findByRegionId(Long regionId);

    void deleteByRegionId(Long regionId);
}
