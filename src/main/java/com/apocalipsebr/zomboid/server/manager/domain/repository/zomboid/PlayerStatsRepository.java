package com.apocalipsebr.zomboid.server.manager.domain.repository.zomboid;

import com.apocalipsebr.zomboid.server.manager.domain.entity.zomboid.PlayerStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerStatsRepository extends JpaRepository<PlayerStats, Long> {
    
    Optional<PlayerStats> findByUsername(String username);
}
