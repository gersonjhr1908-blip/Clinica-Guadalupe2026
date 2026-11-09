package com.clinicaguadalupe.dto;

import jakarta.validation.constraints.NotNull;

public record EstadoUsuarioRequest(

        @NotNull
        Boolean activo
) {
}