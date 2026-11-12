package com.clinicaguadalupe.dto;

import com.clinicaguadalupe.entity.EstadoCita;

import java.time.LocalDateTime;

public record CitaResponse(

        Long id,
        String codigo,
        Long pacienteId,
        String paciente,
        Long medicoId,
        String medico,
        String especialidad,
        Long consultorioId,
        String consultorio,
        LocalDateTime fechaHoraInicio,
        LocalDateTime fechaHoraFin,
        String motivo,
        String observaciones,
        EstadoCita estado,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion
) {
}
