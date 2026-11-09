package com.clinicaguadalupe.dto;

import jakarta.validation.constraints.*;

public record ConsultorioRequest(

        @NotBlank
        @Size(max = 20)
        String codigo,

        @NotBlank
        @Size(max = 100)
        String nombre,

        @NotNull
        @Min(1)
        @Max(20)
        Integer piso,

        @Size(max = 255)
        String descripcion
) {
}