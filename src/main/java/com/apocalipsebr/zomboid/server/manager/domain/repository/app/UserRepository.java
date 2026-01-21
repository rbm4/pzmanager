package com.apocalipsebr.zomboid.server.manager.domain.repository.app;

import com.apocalipsebr.zomboid.server.manager.domain.entity.app.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findBySteamId(String steamId);
    
    Optional<User> findByUsername(String username);
    
    @Query(value = "SELECT u FROM User u WHERE " +
           "CAST(u.steamId AS long) BETWEEN :minSteamId AND :maxSteamId " +
           "LIMIT 1", nativeQuery = false)
    Optional<User> findByApproximateSteamId(@Param("minSteamId") long minSteamId, 
                                             @Param("maxSteamId") long maxSteamId);
}
