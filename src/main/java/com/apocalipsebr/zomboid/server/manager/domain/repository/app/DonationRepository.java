package com.apocalipsebr.zomboid.server.manager.domain.repository.app;

import com.apocalipsebr.zomboid.server.manager.domain.entity.app.Donation;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DonationRepository extends JpaRepository<Donation, Long> {

    Optional<Donation> findByPagbankOrderId(String pagbankOrderId);

    @Query("SELECT d FROM Donation d LEFT JOIN FETCH d.character WHERE d.id = :id")
    Optional<Donation> findByIdEager(Long id);

    List<Donation> findByUser(User user);

    List<Donation> findByStatusAndExpiresAtBefore(String status, LocalDateTime dateTime);
}
