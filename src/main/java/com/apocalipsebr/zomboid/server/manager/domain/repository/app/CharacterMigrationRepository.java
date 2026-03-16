package com.apocalipsebr.zomboid.server.manager.domain.repository.app;

import com.apocalipsebr.zomboid.server.manager.domain.entity.app.CharacterMigration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CharacterMigrationRepository extends JpaRepository<CharacterMigration, Long> {

    Optional<CharacterMigration> findByCharacterId(Long characterId);

    Optional<CharacterMigration> findByCharacterIdAndStatus(Long characterId, String status);

    List<CharacterMigration> findByUserId(Long userId);

    List<CharacterMigration> findByStatus(String status);

    boolean existsByCharacterId(Long characterId);

    void deleteByCharacterId(Long characterId);
}
