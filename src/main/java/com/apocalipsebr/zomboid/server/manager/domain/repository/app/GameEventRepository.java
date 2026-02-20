package com.apocalipsebr.zomboid.server.manager.domain.repository.app;

import com.apocalipsebr.zomboid.server.manager.domain.entity.app.GameEvent;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.GameEvent.EventStatus;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GameEventRepository extends JpaRepository<GameEvent, Long> {

    List<GameEvent> findByStatus(EventStatus status);

    List<GameEvent> findByStatusIn(List<EventStatus> statuses);

    @Query("SELECT e FROM GameEvent e WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(e.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.description) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:status IS NULL OR e.status = :status) " +
           "ORDER BY e.createdAt DESC")
    Page<GameEvent> searchEvents(@Param("search") String search,
                                 @Param("status") EventStatus status,
                                 Pageable pageable);

    @Query("SELECT e FROM GameEvent e ORDER BY " +
           "CASE e.status " +
           "  WHEN 'ACTIVE' THEN 1 " +
           "  WHEN 'FUNDED' THEN 2 " +
           "  WHEN 'PENDING' THEN 3 " +
           "  WHEN 'EXPIRED' THEN 4 " +
           "  WHEN 'CANCELLED' THEN 5 " +
           "END, e.createdAt DESC")
    Page<GameEvent> findAllOrdered(Pageable pageable);

    long countByStatus(EventStatus status);

    long countByCreatedByAndCreatedAtAfter(User createdBy, LocalDateTime createdAfter);

    
}
