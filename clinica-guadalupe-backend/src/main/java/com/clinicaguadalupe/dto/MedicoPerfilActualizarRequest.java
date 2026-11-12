package com.clinicaguadalupe.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/** Datos editables por el propio médico (la contraseña no forma parte del formulario). */
public record MedicoPerfilActualizarRequest(
        @NotBlank String nombres,
        @NotBlank String apellidos,
        @NotBlank @Pattern(regexp = "\\d{8}") String dni,
        @NotBlank @Email String email,
        @NotBlank @Pattern(regexp = "\\d{9}") String telefono,
        @NotNull Long especialidadId,
        @NotBlank @Pattern(regexp = "\\d{5,10}") String cmp,
        @NotNull @DecimalMin("0.01") BigDecimal precioConsulta,
        @NotNull @Min(15) @Max(180) Integer duracionConsultaMinutos,
        @Size(max = 1000) String biografia
) {}
