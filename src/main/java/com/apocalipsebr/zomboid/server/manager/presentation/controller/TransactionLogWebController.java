package com.apocalipsebr.zomboid.server.manager.presentation.controller;

import com.apocalipsebr.zomboid.server.manager.application.service.TransactionLogService;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.TransactionLog;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.User;

import jakarta.servlet.http.HttpSession;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Controller
@RequestMapping("/admin/transactions")
@PreAuthorize("hasRole('ADMIN')")
public class TransactionLogWebController {

    private static final Logger logger = Logger.getLogger(TransactionLogWebController.class.getName());

    private final TransactionLogService transactionLogService;

    public TransactionLogWebController(TransactionLogService transactionLogService) {
        this.transactionLogService = transactionLogService;
    }

    @GetMapping
    public String listTransactions(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<TransactionLog> logsPage = transactionLogService.getTransactions(search, type, pageable);

        model.addAttribute("logs", logsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", logsPage.getTotalPages());
        model.addAttribute("totalTransactions", transactionLogService.getTotalCount());
        model.addAttribute("cashbackCount", transactionLogService.getCashbackCount());
        model.addAttribute("search", search);
        model.addAttribute("type", type);

        return "admin-transactions";
    }

    @PostMapping("/{id}/cashback")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cashbackTransaction(
            @PathVariable Long id,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        try {
            User admin = (User) session.getAttribute("user");
            if (admin == null || !"ADMIN".equals(admin.getRole())) {
                response.put("success", false);
                response.put("message", "Acesso não autorizado");
                return ResponseEntity.status(403).body(response);
            }

            TransactionLog refunded = transactionLogService.cashback(id, admin.getUsername());
            response.put("success", true);
            response.put("message", "Cashback de " + refunded.getAmount() + " ₳ aplicado com sucesso para " + refunded.getCharacterName());
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(400).body(response);
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(404).body(response);
        } catch (Exception e) {
            logger.severe("Error processing cashback: " + e.getMessage());
            response.put("success", false);
            response.put("message", "Erro ao processar cashback. Tente novamente.");
            return ResponseEntity.status(500).body(response);
        }
    }
}
