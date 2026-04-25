package com.apocalipsebr.zomboid.server.manager.domain.repository.app;

import com.apocalipsebr.zomboid.server.manager.domain.entity.app.SafehouseClaimRequest;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.SafehouseClaimStatus;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SafehouseClaimRequestRepository extends JpaRepository<SafehouseClaimRequest, Long> {

    List<SafehouseClaimRequest> findByUserOrderByCreatedAtDesc(User user);

    List<SafehouseClaimRequest> findByStatusOrderByCreatedAtAsc(SafehouseClaimStatus status);

    List<SafehouseClaimRequest> findByStatusOrderByCreatedAtDesc(SafehouseClaimStatus status);
}