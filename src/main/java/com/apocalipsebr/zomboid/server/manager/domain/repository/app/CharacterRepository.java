package com.apocalipsebr.zomboid.server.manager.domain.repository.app;

import com.apocalipsebr.zomboid.server.manager.domain.entity.app.Character;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CharacterRepository extends JpaRepository<Character, Long> {
    
    List<Character> findByUser(User user);
    
    List<Character> findByUserOrderByZombieKillsDesc(User user);
    
    Optional<Character> findByUserAndPlayerName(User user, String playerName);
    
    List<Character> findByIsDeadFalse();
    
    @Query("SELECT c FROM Character c JOIN FETCH c.user WHERE c.isDead = false ORDER BY c.zombieKills DESC LIMIT 10")
    List<Character> findTopActiveCharactersByKills();
}
