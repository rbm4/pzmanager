package com.apocalipsebr.zomboid.server.manager.domain.repository.app;

import com.apocalipsebr.zomboid.server.manager.domain.entity.app.Car;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {
    
    List<Car> findAllByOrderByNameAsc();
    
    List<Car> findByAvailableTrue();
    
    Page<Car> findByAvailableTrue(Pageable pageable);
    
    List<Car> findByNameContainingIgnoreCaseOrModelContainingIgnoreCase(String name, String model);
    
    @Query("SELECT c FROM Car c WHERE " +
           "(:search IS NULL OR :search = '' OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(c.model) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:availableOnly = false OR c.available = true)")
    Page<Car> searchCars(@Param("search") String search, @Param("availableOnly") Boolean availableOnly, Pageable pageable);
}
