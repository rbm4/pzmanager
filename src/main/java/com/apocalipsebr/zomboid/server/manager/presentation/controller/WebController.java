package com.apocalipsebr.zomboid.server.manager.presentation.controller;

import com.apocalipsebr.zomboid.server.manager.application.service.PlayerStatsService;
import com.apocalipsebr.zomboid.server.manager.domain.entity.zomboid.PlayerStats;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

@Controller
public class WebController {

    private final PlayerStatsService playerStatsService;

    public WebController(PlayerStatsService playerStatsService) {
        this.playerStatsService = playerStatsService;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid credentials");
        }
        return "login";
    }

    @PostMapping("/login")
    public String doLogin(@RequestParam String username, 
                         @RequestParam String password,
                         HttpSession session) {
        // Simple authentication - you can enhance this later
        if ("player".equals(username) && "player123".equals(password)) {
            session.setAttribute("user", username);
            session.setAttribute("role", "player");
            return "redirect:/player";
        }
        return "redirect:/login?error";
    }

    @GetMapping("/admin-login")
    public String adminLogin(@RequestParam(required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid admin credentials");
        }
        return "admin-login";
    }

    @PostMapping("/admin-login")
    public String doAdminLogin(@RequestParam String username,
                              @RequestParam String password,
                              HttpSession session) {
        // Admin authentication
        if ("admin".equals(username) && "adminrbz0mb01d2$3".equals(password)) {
            session.setAttribute("user", username);
            session.setAttribute("role", "admin");
            return "redirect:/admin";
        }
        return "redirect:/admin-login?error";
    }

    @GetMapping("/admin")
    public String adminPanel(HttpSession session, Model model) {
        if (!"admin".equals(session.getAttribute("role"))) {
            return "redirect:/admin-login";
        }
        model.addAttribute("username", session.getAttribute("user"));
        return "admin";
    }

    @GetMapping("/player")
    public String playerPanel(HttpSession session, Model model) {
        String role = (String) session.getAttribute("role");
        if (role == null) {
            return "redirect:/login";
        }
        
        String username = (String) session.getAttribute("user");
        PlayerStats stats = new PlayerStats(username);
        
        model.addAttribute("username", username);
        model.addAttribute("stats", stats);
        model.addAttribute("journalCost", playerStatsService.getSkillJournalCost());
        
        return "player";
    }

    @PostMapping("/player/purchase-journal")
    public String purchaseJournal(HttpSession session, Model model) {
        String username = (String) session.getAttribute("user");
        if (username == null) {
            return "redirect:/login";
        }
        
        boolean success = playerStatsService.purchaseSkillJournal(username);
        
        if (success) {
            model.addAttribute("success", "Skill Recovery Journal purchased successfully!");
        } else {
            model.addAttribute("error", "Insufficient currency points or purchase failed.");
        }
        
        return "redirect:/player?purchase=" + (success ? "success" : "error");
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
