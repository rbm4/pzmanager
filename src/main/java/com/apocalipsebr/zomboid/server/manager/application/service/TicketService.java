package com.apocalipsebr.zomboid.server.manager.application.service;

import com.apocalipsebr.zomboid.server.manager.domain.entity.app.Ticket;
import com.apocalipsebr.zomboid.server.manager.domain.repository.app.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

@Service
public class TicketService {
    private static final Logger logger = Logger.getLogger(TicketService.class.getName());
    
    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public List<Ticket> getAllTickets() {
        try {
            List<Ticket> tickets = ticketRepository.findAllByOrderByIdDesc();
            logger.info("Retrieved " + tickets.size() + " tickets from database");
            return tickets;
        } catch (Exception e) {
            logger.severe("Failed to retrieve tickets: " + e.getMessage());
            return List.of();
        }
    }

    public List<Ticket> getTicketsByStatus(String status) {
        try {
            if ("viewed".equalsIgnoreCase(status)) {
                return ticketRepository.findAll().stream()
                    .filter(t -> Boolean.TRUE.equals(t.getViewed()))
                    .toList();
            } else if ("unviewed".equalsIgnoreCase(status)) {
                return ticketRepository.findAll().stream()
                    .filter(t -> Boolean.FALSE.equals(t.getViewed()))
                    .toList();
            } else if ("answered".equalsIgnoreCase(status)) {
                return ticketRepository.findAll().stream()
                    .filter(t -> t.getAnsweredID() != null)
                    .toList();
            }
            return ticketRepository.findAllByOrderByIdDesc();
        } catch (Exception e) {
            logger.severe("Failed to retrieve tickets by status: " + e.getMessage());
            return List.of();
        }
    }

    public Optional<Ticket> getTicketById(Long id) {
        try {
            return ticketRepository.findById(id);
        } catch (Exception e) {
            logger.severe("Failed to retrieve ticket by id: " + e.getMessage());
            return Optional.empty();
        }
    }

    public Map<String, Long> getTicketStatistics() {
        try {
            long total = ticketRepository.count();
            long viewed = ticketRepository.findAll().stream()
                .filter(t -> Boolean.TRUE.equals(t.getViewed()))
                .count();
            long unviewed = total - viewed;
            long answered = ticketRepository.findAll().stream()
                .filter(t -> t.getAnsweredID() != null)
                .count();

            return Map.of(
                "total", total,
                "viewed", viewed,
                "unviewed", unviewed,
                "answered", answered
            );
        } catch (Exception e) {
            logger.severe("Failed to get ticket statistics: " + e.getMessage());
            return Map.of("total", 0L, "viewed", 0L, "unviewed", 0L, "answered", 0L);
        }
    }
}
