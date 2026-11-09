package com.clinicaguadalupe.dto;

import com.clinicaguadalupe.entity.Rol;
import java.time.LocalDateTime;

public record UsuarioResponse(
        Long id,
        String nombres,
        String apellidos,
        String dni,
        String email,
        String telefono,
        Rol rol,
        Boolean activo,
        LocalDateTime fechaCreacion
) {
}