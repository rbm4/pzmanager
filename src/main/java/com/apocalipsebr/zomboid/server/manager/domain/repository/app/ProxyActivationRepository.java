package com.apocalipsebr.zomboid.server.manager.domain.repository.app;

import com.apocalipsebr.zomboid.server.manager.domain.entity.app.ProxyActivation;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProxyActivationRepository extends JpaRepository<ProxyActivation, Long> {

    Optional<ProxyActivation> findByProxyIdAndStatusIn(String proxyId, List<String> statuses);

    List<ProxyActivation> findByStatusAndExpiresAtBefore(String status, LocalDateTime now);

    List<ProxyActivation> findByStatus(String status);

    List<ProxyActivation> findByStatusIn(List<String> statuses);

    List<ProxyActivation> findByStatusAndActivatedAtBefore(String status, LocalDateTime cutoff);

    Page<ProxyActivation> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
}
