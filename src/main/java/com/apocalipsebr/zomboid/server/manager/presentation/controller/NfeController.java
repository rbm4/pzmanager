package com.apocalipsebr.zomboid.server.manager.presentation.controller;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.apocalipsebr.zomboid.server.manager.application.service.NfeService;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.NfeEmission;
import com.apocalipsebr.zomboid.server.manager.domain.entity.app.User;
import com.apocalipsebr.zomboid.server.manager.presentation.dto.nfe.NfeEmissionRequestDTO;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/nfe")
@PreAuthorize("hasRole('ADMIN')")
public class NfeController {

    private static final Logger logger = LoggerFactory.getLogger(NfeController.class);

    private final NfeService nfeService;

    public NfeController(NfeService nfeService) {
        this.nfeService = nfeService;
    }

    /**
     * Lista todas as NF-e emitidas.
     */
    @GetMapping
    public String listEmissions(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null)
            return "redirect:/login";

        model.addAttribute("emissions", nfeService.listarEmissoes());
        model.addAttribute("configured", nfeService.isConfigured());
        return "nfe-list";
    }

    /**
     * Formulário de emissão de NF-e.
     */
    @GetMapping("/emit")
    public String emitForm(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null)
            return "redirect:/login";

        if (!nfeService.isConfigured()) {
            model.addAttribute("error",
                    "NFe não está configurada. Configure o certificado digital e dados do emitente no application.properties.");
            return "nfe-emit";
        }

        model.addAttribute("configured", true);
        return "nfe-emit";
    }

    /**
     * Processa a emissão de uma NF-e.
     */
    @PostMapping("/emit")
    @ResponseBody
    public ResponseEntity<?> emitNfe(@RequestBody NfeEmissionRequestDTO request, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Você precisa estar logado."));
        }

        try {
            NfeEmission emission = nfeService.emitirNfe(request, user);
            return ResponseEntity.ok(Map.of(
                    "id", emission.getId(),
                    "status", emission.getStatus(),
                    "numeroNota", emission.getNumeroNota(),
                    "serie", emission.getSerie(),
                    "chaveAcesso", emission.getChaveAcesso() != null ? emission.getChaveAcesso() : "",
                    "protocolo", emission.getProtocolo() != null ? emission.getProtocolo() : "",
                    "statusSefaz", emission.getStatusSefaz() != null ? emission.getStatusSefaz() : "",
                    "motivoSefaz", emission.getMotivoSefaz() != null ? emission.getMotivoSefaz() : ""));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Erro ao emitir NF-e", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro ao emitir NF-e: " + e.getMessage()));
        }
    }

    /**
     * Detalhes de uma NF-e.
     */
    @GetMapping("/{id}")
    public String emission(@PathVariable Long id, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null)
            return "redirect:/login";

        NfeEmission emission = nfeService.buscarEmissao(id)
                .orElse(null);
        if (emission == null)
            return "redirect:/nfe";

        model.addAttribute("emission", emission);
        return "nfe-detail";
    }

    /**
     * Cancela uma NF-e autorizada.
     */
    @PostMapping("/{id}/cancel")
    @ResponseBody
    public ResponseEntity<?> cancelNfe(@PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Não autorizado."));
        }

        String motivo = body.get("motivo");
        try {
            NfeEmission emission = nfeService.cancelarNota(id, motivo);
            return ResponseEntity.ok(Map.of(
                    "status", emission.getStatus(),
                    "message", "NF-e cancelada com sucesso."));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Erro ao cancelar NF-e", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro ao cancelar: " + e.getMessage()));
        }
    }

    /**
     * Carta de correção.
     */
    @PostMapping("/{id}/correct")
    @ResponseBody
    public ResponseEntity<?> correctNfe(@PathVariable Long id,
            @RequestBody Map<String, String> body,
            HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Não autorizado."));
        }

        String correcao = body.get("correcao");
        try {
            String resultado = nfeService.corrigirNota(id, correcao);
            return ResponseEntity.ok(Map.of(
                    "message", "Carta de correção enviada com sucesso.",
                    "resultado", resultado));
        } catch (Exception e) {
            logger.error("Erro ao enviar carta de correção", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro: " + e.getMessage()));
        }
    }

    /**
     * Consulta status da SEFAZ.
     */
    @GetMapping("/status-sefaz")
    @ResponseBody
    public ResponseEntity<?> statusSefaz() {
        String status = nfeService.consultarStatusSefaz();
        return ResponseEntity.ok(Map.of("status", status));
    }

    /**
     * Download do XML da NF-e.
     */
    @GetMapping("/{id}/xml")
    public ResponseEntity<byte[]> downloadXml(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        NfeEmission emission = nfeService.buscarEmissao(id).orElse(null);
        if (emission == null) {
            return ResponseEntity.notFound().build();
        }

        String xml = emission.getXmlNotaProcessada() != null
                ? emission.getXmlNotaProcessada()
                : emission.getXmlEnvio();

        if (xml == null) {
            return ResponseEntity.notFound().build();
        }

        String filename = "NFe_" + emission.getNumeroNota() + "_" + emission.getSerie() + ".xml";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_XML)
                .body(xml.getBytes(StandardCharsets.UTF_8));
    }
}
