package com.apocalipsebr.zomboid.server.manager.domain.repository.app;

import com.apocalipsebr.zomboid.server.manager.domain.entity.app.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    
    List<Ticket> findAllByOrderByIdDesc();
}
