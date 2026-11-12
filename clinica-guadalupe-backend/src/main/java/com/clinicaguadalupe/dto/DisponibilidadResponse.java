package com.clinicaguadalupe.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record DisponibilidadResponse(

        Long horarioId,
        Long consultorioId,
        String consultorio,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin
) {
}