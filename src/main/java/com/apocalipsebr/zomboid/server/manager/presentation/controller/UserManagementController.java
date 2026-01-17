package com.apocalipsebr.zomboid.server.manager.presentation.controller;

import com.apocalipsebr.zomboid.server.manager.application.service.UserService;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/users")
public class UserManagementController {

    private final UserService userService;

    public UserManagementController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public String listUsers(HttpSession session, Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        model.addAttribute("username", ((User) session.getAttribute("user")).getUsername());
        return "admin-users";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{userId}/role")
    @ResponseBody
    public ResponseEntity<?> updateUserRole(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request) {
        try {
            String role = request.get("role");
            if (role == null || (!role.equals("ADMIN") && !role.equals("PLAYER"))) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid role. Must be ADMIN or PLAYER"));
            }
            
            User user = userService.updateUserRole(userId, role);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User role updated successfully",
                "user", Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "role", user.getRole()
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }
}
