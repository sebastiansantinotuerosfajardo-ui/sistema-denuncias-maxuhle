package com.maxuhle.denuncias.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DenunciaUpdateRequest(
        @NotBlank(message = "La descripción es obligatoria")
        @Size(max = 2000, message = "La descripción no puede superar 2000 caracteres") String descripcion) {
}
