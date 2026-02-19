package com.apocalipsebr.zomboid.server.manager.domain.repository.app;

import com.apocalipsebr.zomboid.server.manager.domain.entity.app.TransactionLog;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionLogRepository extends JpaRepository<TransactionLog, Long> {

    @Query("SELECT t FROM TransactionLog t WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(t.playerUsername) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.characterName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.itemName) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:type IS NULL OR :type = '' OR t.transactionType = :type)")
    Page<TransactionLog> search(@Param("search") String search, @Param("type") String type, Pageable pageable);

    long countByCashbackTrue();
}
