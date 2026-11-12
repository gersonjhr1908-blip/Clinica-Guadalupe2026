package com.clinicaguadalupe.dto;

import java.time.LocalDateTime;

public record ConsultorioResponse(

        Long id,
        String codigo,
        String nombre,
        Integer piso,
        String descripcion,
        Boolean activo,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion
) {
}