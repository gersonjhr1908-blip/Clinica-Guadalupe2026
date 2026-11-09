package com.clinicaguadalupe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambiarPasswordRequest(

        @NotBlank
        @Size(min = 8, message = "La contraseña debe tener mínimo 8 caracteres")
        String nuevaPassword
) {
}