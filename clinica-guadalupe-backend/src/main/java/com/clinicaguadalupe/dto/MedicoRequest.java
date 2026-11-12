package com.clinicaguadalupe.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record MedicoRequest(

        @NotBlank
        @Size(max = 100)
        String nombres,

        @NotBlank
        @Size(max = 100)
        String apellidos,

        @NotBlank
        @Pattern(regexp = "\\d{8}", message = "El DNI debe tener 8 números")
        String dni,

        @NotBlank
        @Email
        String email,

        @NotBlank
        @Pattern(regexp = "\\d{9}", message = "El teléfono debe tener 9 números")
        String telefono,

        @NotBlank
        @Size(min = 8, message = "La contraseña debe tener mínimo 8 caracteres")
        String password,

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