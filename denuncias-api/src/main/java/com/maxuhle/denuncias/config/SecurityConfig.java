package com.maxuhle.denuncias.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Seguridad del Avance 2: HTTP Basic (sin JWT), usuarios en la tabla "usuario",
 * contraseñas con BCrypt y dos roles: ADMIN y OPERADOR.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Público
                        .requestMatchers("/api/publico/**").permitAll()
                        // Solo ADMIN: reportes, seguimiento, edición y eliminación
                        .requestMatchers(HttpMethod.GET, "/api/denuncias/estadisticas/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/denuncias/protocolo-pendiente").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/denuncias/*/seguimientos").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/denuncias/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/denuncias/**").hasRole("ADMIN")
                        // ADMIN y OPERADOR: registrar y consultar
                        .requestMatchers(HttpMethod.POST, "/api/denuncias").hasAnyRole("ADMIN", "OPERADOR")
                        .requestMatchers(HttpMethod.GET, "/api/denuncias/**").hasAnyRole("ADMIN", "OPERADOR")
                        // Todo lo demás requiere estar autenticado
                        .anyRequest().authenticated())
                // Cabeceras básicas: nosniff, no incrustar en iframes, sin Referer y HSTS (solo sobre HTTPS)
                .headers(h -> h
                        .contentTypeOptions(Customizer.withDefaults())
                        .frameOptions(f -> f.deny())
                        .referrerPolicy(r -> r.policy(ReferrerPolicy.NO_REFERRER))
                        .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).maxAgeInSeconds(31536000)))
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    /** Orígenes permitidos (lista separada por comas) desde CORS_ALLOWED_ORIGINS; por defecto, Angular en local. */
    @Value("${cors.allowed-origins:http://localhost:4200}")
    private String origenesPermitidos;

    /** Avance 3: permite que Angular consuma la API desde otro origen. Nunca se admite "*". */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        List<String> origenes = Arrays.stream(origenesPermitidos.split(","))
                .map(String::trim)
                .filter(o -> !o.isEmpty())
                .toList();
        if (origenes.isEmpty() || origenes.contains("*")) {
            throw new IllegalStateException("CORS_ALLOWED_ORIGINS debe listar orígenes concretos (sin \"*\")");
        }
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(origenes);
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/api/**", cfg);
        return src;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
