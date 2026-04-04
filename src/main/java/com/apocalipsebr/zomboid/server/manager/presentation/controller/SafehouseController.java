package com.apocalipsebr.zomboid.server.manager.presentation.controller;

import com.apocalipsebr.zomboid.server.manager.application.service.SafehouseService;
import com.apocalipsebr.zomboid.server.manager.application.service.SafehouseService.SafehouseListResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequestMapping("/admin/safehouses")
public class SafehouseController {

    private static final Logger log = LoggerFactory.getLogger(SafehouseController.class);

    private final SafehouseService safehouseService;

    public SafehouseController(SafehouseService safehouseService) {
        this.safehouseService = safehouseService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String safehousesPage() {
        return "safehouses";
    }

    @GetMapping("/api/list")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> listSafehouses() {
        SafehouseListResult result = safehouseService.listSafehouses();

        List<Map<String, Object>> safehousesJson = new ArrayList<>();
        for (var sh : result.safehouses()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("x", sh.x());
            m.put("y", sh.y());
            m.put("w", sh.w());
            m.put("h", sh.h());
            m.put("owner", sh.owner());
            m.put("town", sh.town());
            m.put("name", sh.name());
            m.put("members", sh.members());
            safehousesJson.add(m);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("safehouses", safehousesJson);
        response.put("count", result.safehouses().size());
        response.put("warnings", result.warnings());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StreamingResponseBody> exportSafehouses(
            @RequestParam(defaultValue = "2") int margin) {

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmm"));
        String filename = "safehouses-export-" + timestamp + ".zip";

        StreamingResponseBody body = outputStream -> {
            try {
                safehouseService.exportSafehouseBinsAsZip(margin, outputStream);
            } catch (IllegalStateException e) {
                log.error("Safehouse export failed: {}", e.getMessage());
                throw e;
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/zip"))
                .body(body);
    }
}
