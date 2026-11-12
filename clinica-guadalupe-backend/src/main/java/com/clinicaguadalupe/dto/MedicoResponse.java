package com.clinicaguadalupe.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MedicoResponse(

        Long id,
        Long usuarioId,
        String nombres,
        String apellidos,
        String dni,
        String email,
        String telefono,
        Long especialidadId,
        String especialidad,
        String cmp,
        BigDecimal precioConsulta,
        Integer duracionConsultaMinutos,
        String biografia,
        Boolean activo,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion
) {
}