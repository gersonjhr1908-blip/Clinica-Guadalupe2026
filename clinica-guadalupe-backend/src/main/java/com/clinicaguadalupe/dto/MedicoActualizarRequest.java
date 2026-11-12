package com.clinicaguadalupe.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record MedicoActualizarRequest(

        @NotNull
        Long especialidadId,

        @NotBlank
        @Pattern(
                regexp = "\\d{5,10}",
                message = "El CMP debe contener entre 5 y 10 números"
        )
        String cmp,

        @NotNull
        @DecimalMin(value = "0.01")
        BigDecimal precioConsulta,

        @NotNull
        @Min(15)
        @Max(180)
        Integer duracionConsultaMinutos,

        @Size(max = 1000)
        String biografia
) {
}