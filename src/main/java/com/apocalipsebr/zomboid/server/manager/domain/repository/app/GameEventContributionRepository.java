package com.apocalipsebr.zomboid.server.manager.domain.repository.app;

import com.apocalipsebr.zomboid.server.manager.domain.entity.app.GameEventContribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameEventContributionRepository extends JpaRepository<GameEventContribution, Long> {

    List<GameEventContribution> findByGameEventId(Long gameEventId);

    List<GameEventContribution> findByUserId(Long userId);

    @Query("SELECT COALESCE(SUM(c.amount), 0) FROM GameEventContribution c WHERE c.gameEvent.id = :eventId AND c.user.id = :userId")
    int sumByEventAndUser(@Param("eventId") Long eventId, @Param("userId") Long userId);
}
