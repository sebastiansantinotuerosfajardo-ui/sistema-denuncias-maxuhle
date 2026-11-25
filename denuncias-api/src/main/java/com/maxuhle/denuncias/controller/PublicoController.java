package com.maxuhle.denuncias.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Endpoint público (no requiere autenticación). */
@RestController
@RequestMapping("/api/publico")
public class PublicoController {

    @GetMapping("/info")
    public Map<String, String> info() {
        return Map.of(
                "sistema", "API de Denuncias - Colegio Max Uhle (Arequipa)",
                "version", "0.2.0",
                "estado", "operativo");
    }
}
