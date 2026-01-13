package com.apocalipsebr.zomboid.server.manager.presentation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

@Controller
public class WebController {

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
        model.addAttribute("username", session.getAttribute("user"));
        return "player";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
