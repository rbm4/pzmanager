package com.apocalipsebr.zomboid.server.manager.application.service;

import com.apocalipsebr.zomboid.server.manager.domain.entity.app.ClaimedCarItem;
import com.apocalipsebr.zomboid.server.manager.domain.repository.app.ClaimedCarItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClaimedCarItemService {

    private final ClaimedCarItemRepository claimedCarItemRepository;

    public ClaimedCarItemService(ClaimedCarItemRepository claimedCarItemRepository) {
        this.claimedCarItemRepository = claimedCarItemRepository;
    }

    public List<ClaimedCarItem> getItemsByClaimedCarId(Long claimedCarId) {
        return claimedCarItemRepository.findByClaimedCarId(claimedCarId);
    }
}
