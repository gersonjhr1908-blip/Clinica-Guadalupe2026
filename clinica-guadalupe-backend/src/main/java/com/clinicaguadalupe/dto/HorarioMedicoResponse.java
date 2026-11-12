package com.clinicaguadalupe.dto;

import com.clinicaguadalupe.entity.DiaSemana;

import java.time.LocalDateTime;
import java.time.LocalTime;

public record HorarioMedicoResponse(

        Long id,
        Long medicoId,
        String medico,
        Long consultorioId,
        String consultorio,
        DiaSemana diaSemana,
        LocalTime horaInicio,
        LocalTime horaFin,
        Boolean activo,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion
) {
}