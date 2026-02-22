package com.apocalipsebr.zomboid.server.manager.presentation.controller;

import com.apocalipsebr.zomboid.server.manager.application.service.DonationService;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.User;
import com.apocalipsebr.zomboid.server.manager.domain.repository.app.UserRepository;
import com.apocalipsebr.zomboid.server.manager.presentation.dto.pagbank.DonationRequestDTO;
import com.apocalipsebr.zomboid.server.manager.presentation.dto.pagbank.DonationStatusDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for PIX donations via PagBank.
 * Handles the donation page, QR code generation, status polling, and webhook
 * notifications.
 */
@Controller
@RequestMapping("/donations")
public class DonationController {

    private static final Logger logger = Logger.getLogger(DonationController.class.getName());

    private final DonationService donationService;
    private final UserRepository userRepository;

    public DonationController(DonationService donationService, UserRepository userRepository) {
        this.donationService = donationService;
        this.userRepository = userRepository;
    }

    /**
     * Renders the donation page.
     */
    @GetMapping
    public String donationPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        model.addAttribute("username", user.getUsername());

        // Dynamic coin rate for the view
        int coinRatePerCentavo = donationService.getCoinRatePerCentavo();
        int coinRatePerReal = coinRatePerCentavo * 100;
        model.addAttribute("coinRatePerCentavo", coinRatePerCentavo);
        model.addAttribute("coinRatePerReal", coinRatePerReal);

        // Pre-fill saved PagBank info if user chose to remember
        model.addAttribute("savedEmail", user.getPagbankEmail() != null ? user.getPagbankEmail() : "");
        model.addAttribute("savedCpf", user.getPagbankCpf() != null ? user.getPagbankCpf() : "");

        return "donations";
    }

    /**
     * Creates a new PIX donation - generates QR code.
     */
    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity<?> createDonation(@RequestBody DonationRequestDTO request, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Você precisa estar logado para fazer uma doação."));
        }

        if (request.getAmountCentavos() < 100) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "O valor mínimo para doação é R$1,00."));
        }

        // Validate email and CPF
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "O e-mail é obrigatório para gerar o PIX."));
        }
        if (request.getCpf() == null || request.getCpf().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "O CPF é obrigatório para gerar o PIX."));
        }

        // Strip non-digits from CPF
        String cpf = request.getCpf().replaceAll("\\D", "");
        if (cpf.length() != 11) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "CPF inválido. Informe os 11 dígitos."));
        }

        try {
            DonationStatusDTO status = donationService.createDonation(
                    user, request.getAmountCentavos(), request.getEmail().trim(), cpf, request.isRememberInfo());
            return ResponseEntity.ok(status);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to create donation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro ao criar o PIX. Tente novamente."));
        }
    }

    /**
     * Checks donation payment status (polled every 10 seconds from frontend).
     */
    @GetMapping("/status/{donationId}")
    @ResponseBody
    public ResponseEntity<?> checkStatus(@PathVariable Long donationId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Não autorizado."));
        }

        try {
            DonationStatusDTO status = donationService.checkDonationStatus(donationId, user);
            return ResponseEntity.ok(status);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Webhook endpoint for PagBank notifications (no auth required).
     */
    @PostMapping("/webhook")
    @ResponseBody
    public ResponseEntity<Void> webhook(@RequestParam("notificationCode") String notificationCode,
            @RequestParam("notificationType") String notificationType, @RequestBody String body) {
        donationService.processWebhook(notificationCode,notificationType);
        return ResponseEntity.noContent().build();
    }
}
