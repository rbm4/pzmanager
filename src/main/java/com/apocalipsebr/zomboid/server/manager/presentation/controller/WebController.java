package com.apocalipsebr.zomboid.server.manager.presentation.controller;

import com.apocalipsebr.zomboid.server.manager.application.service.CharacterService;
import com.apocalipsebr.zomboid.server.manager.application.service.PlayerStatsService;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.Character;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.PlayerStats;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.User;
import com.apocalipsebr.zomboid.server.manager.domain.repository.app.UserRepository;

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
    private final UserRepository userRepository;

    public WebController(PlayerStatsService playerStatsService, CharacterService characterService, UserRepository userRepository) {
        this.playerStatsService = playerStatsService;
        this.characterService = characterService;
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String home() {
        // Show index page for everyone (public landing page)
        return "index";
    }
    
    @GetMapping("/index")
    public String index() {
        return "index";
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
        var sessionUser = (User) session.getAttribute("user");
        if (sessionUser == null) {
            return "redirect:/login";
        }
        
        // Refresh user from database to get updated character currency points
        User user = userRepository.findById(sessionUser.getId())
            .orElseThrow(() -> new IllegalStateException("User not found"));
        
        // Update session with refreshed user
        session.setAttribute("user", user);
        
        PlayerStats stats = new PlayerStats(user.getUsername());
        
        // Get user's characters
        List<Character> userCharacters = characterService.getUserCharacters(user);
        
        // Calculate totals from the fetched characters to avoid LazyInitializationException
        int totalKills = userCharacters.stream()
            .mapToInt(Character::getZombieKills)
            .sum();
        int totalPoints = userCharacters.stream()
            .mapToInt(Character::getCurrencyPoints)
            .sum();
        
        // Get top characters rankings
        List<Character> topCharactersByKills = characterService.getTopCharactersByKills();
        List<Character> topCharactersByHours = characterService.getTopCharactersByHoursSurvived();
        
        model.addAttribute("username", user.getUsername());
        model.addAttribute("role", user.getRole());
        model.addAttribute("stats", stats);
        model.addAttribute("journalCost", playerStatsService.getSkillJournalCost());
        model.addAttribute("userCharacters", userCharacters);
        model.addAttribute("topCharactersByKills", topCharactersByKills);
        model.addAttribute("topCharactersByHours", topCharactersByHours);
        model.addAttribute("totalKills", totalKills);
        model.addAttribute("totalPoints", totalPoints);
        
        return "player";
    }
    
    @GetMapping("/my-characters")
    public String myCharacters(HttpSession session, Model model) {
        var user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        // Get user's characters
        List<Character> userCharacters = characterService.getUserCharacters(user);
        
        model.addAttribute("username", user.getUsername());
        model.addAttribute("role", user.getRole());
        model.addAttribute("userCharacters", userCharacters);
        
        return "my-characters";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
