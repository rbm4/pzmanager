package com.apocalipsebr.zomboid.server.manager.domain.repository.zomboid;

import com.apocalipsebr.zomboid.server.manager.domain.entity.zomboid.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    
    List<Ticket> findAllByOrderByIdDesc();
}
