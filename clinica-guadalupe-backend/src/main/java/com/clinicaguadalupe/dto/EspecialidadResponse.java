package com.clinicaguadalupe.dto;

import java.time.LocalDateTime;

public record EspecialidadResponse(
        Long id,
        String nombre,
        String descripcion,
        Boolean activo,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion
) {
}