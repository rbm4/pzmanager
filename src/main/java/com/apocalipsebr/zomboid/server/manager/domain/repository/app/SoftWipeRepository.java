package com.apocalipsebr.zomboid.server.manager.domain.repository.app;

import com.apocalipsebr.zomboid.server.manager.domain.entity.app.SoftWipe;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.SoftWipeStatus;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.User;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SoftWipeRepository extends JpaRepository<SoftWipe, Long> {

    List<SoftWipe> findByStatus(SoftWipeStatus status);

    List<SoftWipe> findByStatusIn(List<SoftWipeStatus> statuses);

    List<SoftWipe> findByUserOrderByCreatedAtDesc(User user);

    List<SoftWipe> findByUser(User user);

    @Modifying
    @Query("UPDATE SoftWipe sw SET sw.status = :newStatus WHERE sw.status = :oldStatus")
    @Transactional
    int updateStatusBatch(@Param("oldStatus") SoftWipeStatus oldStatus,
                          @Param("newStatus") SoftWipeStatus newStatus);
}
