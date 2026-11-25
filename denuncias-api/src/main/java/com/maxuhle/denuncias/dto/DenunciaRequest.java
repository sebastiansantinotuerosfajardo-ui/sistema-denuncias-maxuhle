package com.maxuhle.denuncias.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DenunciaRequest(
        @NotBlank(message = "El tipo de denuncia es obligatorio") String tipoDenuncia,
        @NotBlank(message = "La descripción es obligatoria")
        @Size(max = 2000, message = "La descripción no puede superar 2000 caracteres") String descripcion,
        String nombreDenunciante,
        @NotBlank(message = "El nombre del afectado es obligatorio") String nombreAfectado) {
}
