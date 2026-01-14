package com.apocalipsebr.zomboid.server.manager.presentation.controller;

import com.apocalipsebr.zomboid.server.manager.application.service.TicketService;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.Ticket;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/tickets")
public class TicketController {
    
    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    public String viewTickets(@RequestParam(required = false) String status, 
                             HttpSession session, 
                             Model model) {
        // Check if user is admin
        if (!"admin".equals(session.getAttribute("role"))) {
            return "redirect:/admin-login";
        }

        List<Ticket> tickets;
        if (status != null && !status.isEmpty()) {
            tickets = ticketService.getTicketsByStatus(status);
            model.addAttribute("filterStatus", status);
        } else {
            tickets = ticketService.getAllTickets();
        }

        Map<String, Long> stats = ticketService.getTicketStatistics();

        model.addAttribute("tickets", tickets);
        model.addAttribute("stats", stats);
        model.addAttribute("username", session.getAttribute("user"));

        return "tickets";
    }
}
