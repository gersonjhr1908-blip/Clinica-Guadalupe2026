package com.clinicaguadalupe.dto;

import com.clinicaguadalupe.entity.Rol;

public record LoginResponse(
        String token,
        String tipo,
        Long usuarioId,
        String nombreCompleto,
        String email,
        Rol rol
) {
}