package com.maxuhle.denuncias.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SeguimientoRequest(
        @NotBlank(message = "El nuevo estado es obligatorio")
        @Pattern(regexp = "RECIBIDA|PROTOCOLO_ACTIVADO|EN_ATENCION|DERIVADA|CERRADA",
                message = "Estado no válido. Use: RECIBIDA, PROTOCOLO_ACTIVADO, EN_ATENCION, DERIVADA o CERRADA")
        String estadoNuevo,
        @Size(max = 1000, message = "La observación no puede superar 1000 caracteres") String observacion) {
}
