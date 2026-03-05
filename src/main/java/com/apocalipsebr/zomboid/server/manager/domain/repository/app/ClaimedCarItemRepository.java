package com.apocalipsebr.zomboid.server.manager.domain.repository.app;

import com.apocalipsebr.zomboid.server.manager.domain.entity.app.ClaimedCarItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimedCarItemRepository extends JpaRepository<ClaimedCarItem, Long> {

    List<ClaimedCarItem> findByClaimedCarId(Long claimedCarId);

    void deleteByClaimedCarId(Long claimedCarId);
}
