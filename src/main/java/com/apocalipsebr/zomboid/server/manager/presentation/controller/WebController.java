package com.apocalipsebr.zomboid.server.manager.presentation.controller;

import com.apocalipsebr.zomboid.server.manager.application.service.CharacterService;
import com.apocalipsebr.zomboid.server.manager.application.service.PlayerStatsService;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.Character;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.PlayerStats;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.User;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
public class WebController {

    private final PlayerStatsService playerStatsService;
    private final CharacterService characterService;

    public WebController(PlayerStatsService playerStatsService, CharacterService characterService) {
        this.playerStatsService = playerStatsService;
        this.characterService = characterService;
    }

    @GetMapping("/")
    public String home(HttpSession session) {
        var user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        // Redirect authenticated users to their player panel
        return "redirect:/player";
    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid credentials");
        }
        return "login";
    }

    @GetMapping("/admin")
    public String adminPanel(HttpSession session, Model model) {
        var user = (User) session.getAttribute("user");
        if (user == null || !"ADMIN".equals(user.getRole())) {
            return "redirect:/login";
        }
        model.addAttribute("username", user.getUsername());
        return "admin";
    }

    @GetMapping("/player")
    public String playerPanel(HttpSession session, Model model) {
        var user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        PlayerStats stats = new PlayerStats(user.getUsername());
        
        // Get user's characters
        List<Character> userCharacters = characterService.getUserCharacters(user);
        
        // Get top characters ranking
        List<Character> topCharacters = characterService.getTopCharactersByKills();
        
        model.addAttribute("username", user.getUsername());
        model.addAttribute("role", user.getRole());
        model.addAttribute("stats", stats);
        model.addAttribute("journalCost", playerStatsService.getSkillJournalCost());
        model.addAttribute("userCharacters", userCharacters);
        model.addAttribute("topCharacters", topCharacters);
        model.addAttribute("totalKills", user.getTotalZombieKills());
        model.addAttribute("totalPoints", user.getTotalCurrencyPoints());
        
        return "player";
    }

    @PostMapping("/player/purchase-journal")
    public String purchaseJournal(HttpSession session, Model model) {
        var user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        boolean success = playerStatsService.purchaseSkillJournal(user.getUsername());
        
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
