package com.apocalipsebr.zomboid.server.manager.domain.repository.app;

import com.apocalipsebr.zomboid.server.manager.domain.entity.app.ClaimedCar;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClaimedCarRepository extends JpaRepository<ClaimedCar, Long> {

    Optional<ClaimedCar> findByVehicleHash(String vehicleHash);

    List<ClaimedCar> findByUser(User user);

    List<ClaimedCar> findByUserIdOrderByUpdatedAtDesc(Long userId);

    List<ClaimedCar> findByMigratedFalse();

    List<ClaimedCar> findByXNotNullAndYNotNull();
}
