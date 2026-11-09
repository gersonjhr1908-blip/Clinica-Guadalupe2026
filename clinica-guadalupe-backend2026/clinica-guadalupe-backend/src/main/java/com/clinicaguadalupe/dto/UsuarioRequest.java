package com.clinicaguadalupe.dto;

import com.clinicaguadalupe.entity.Rol;
import jakarta.validation.constraints.*;

public record UsuarioRequest(

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
        Rol rol
) {
}