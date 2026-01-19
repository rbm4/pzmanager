package com.apocalipsebr.zomboid.server.manager.domain.repository.player;

import com.apocalipsebr.zomboid.server.manager.domain.entity.player.NetworkPlayer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NetworkPlayerRepository extends JpaRepository<NetworkPlayer, Integer> {
    
    Optional<NetworkPlayer> findBySteamid(String steamid);
    
    @Query("SELECT np FROM NetworkPlayer np WHERE " +
           "LOWER(np.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(np.steamid) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<NetworkPlayer> searchByNameOrSteamId(@Param("search") String search, Pageable pageable);
    
    Page<NetworkPlayer> findAll(Pageable pageable);
}
