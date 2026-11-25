package com.maxuhle.denuncias.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

/** Usuario del sistema. La contraseña se guarda cifrada con BCrypt. */
@Entity
@Getter
@Setter
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false, length = 100)
    private String password;

    /** ADMIN u OPERADOR (sin el prefijo ROLE_). */
    @Column(nullable = false)
    private String rol;
}
