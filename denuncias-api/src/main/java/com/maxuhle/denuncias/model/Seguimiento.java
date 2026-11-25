package com.maxuhle.denuncias.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Historial de atención de una denuncia.
 * Relaciones: N:1 con Denuncia y N:1 con Usuario (quien registró el cambio).
 */
@Entity
@Getter
@Setter
public class Seguimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "denuncia_id")
    @JsonIgnore
    private Denuncia denuncia;

    // Se oculta del JSON para no exponer la contraseña cifrada; se publica solo el username
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "usuario_id")
    @JsonIgnore
    private Usuario usuario;

    private String estadoAnterior;

    private String estadoNuevo;

    @Column(length = 1000)
    private String observacion;

    private LocalDateTime fecha;

    @PrePersist
    public void alRegistrar() {
        this.fecha = LocalDateTime.now();
    }

    @JsonProperty("denunciaId")
    public Long getDenunciaId() {
        return denuncia != null ? denuncia.getId() : null;
    }

    @JsonProperty("responsable")
    public String getResponsable() {
        return usuario != null ? usuario.getUsername() : null;
    }
}
