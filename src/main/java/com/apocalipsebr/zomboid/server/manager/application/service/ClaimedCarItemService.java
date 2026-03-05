package com.apocalipsebr.zomboid.server.manager.application.service;

import com.apocalipsebr.zomboid.server.manager.domain.entity.app.ClaimedCarItem;
import com.apocalipsebr.zomboid.server.manager.domain.repository.app.ClaimedCarItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;

@Service
public class ClaimedCarItemService {

    private static final Logger logger = Logger.getLogger(ClaimedCarItemService.class.getName());

    private final ClaimedCarItemRepository claimedCarItemRepository;

    public ClaimedCarItemService(ClaimedCarItemRepository claimedCarItemRepository) {
        this.claimedCarItemRepository = claimedCarItemRepository;
    }

    public List<ClaimedCarItem> getItemsByClaimedCarId(Long claimedCarId) {
        return claimedCarItemRepository.findByClaimedCarId(claimedCarId);
    }
}
