package com.apocalipsebr.zomboid.server.manager.domain.repository.app;

import com.apocalipsebr.zomboid.server.manager.domain.entity.app.NfeEmission;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NfeEmissionRepository extends JpaRepository<NfeEmission, Long> {

    List<NfeEmission> findByUserOrderByCreatedAtDesc(User user);

    List<NfeEmission> findAllByOrderByCreatedAtDesc();

    Optional<NfeEmission> findByChaveAcesso(String chaveAcesso);

    Optional<NfeEmission> findByNumeroRecibo(String numeroRecibo);

    @Query("SELECT COALESCE(MAX(CAST(n.numeroNota AS int)), 0) FROM NfeEmission n WHERE n.serie = :serie")
    int findMaxNumeroNotaBySerie(String serie);

    List<NfeEmission> findByStatus(String status);
}
