package com.apocalipsebr.zomboid.server.manager.domain.repository.app;

import com.apocalipsebr.zomboid.server.manager.domain.entity.app.GameEventProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameEventPropertyRepository extends JpaRepository<GameEventProperty, Long> {

    List<GameEventProperty> findByGameEventId(Long gameEventId);

    List<GameEventProperty> findByPropertyTarget(String propertyTarget);

    List<GameEventProperty> findByLinkedRegionIdNotNull();
}
