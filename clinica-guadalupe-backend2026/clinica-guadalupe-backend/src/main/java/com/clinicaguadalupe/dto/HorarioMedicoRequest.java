package com.clinicaguadalupe.dto;

import com.clinicaguadalupe.entity.DiaSemana;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record HorarioMedicoRequest(

        @NotNull
        Long medicoId,

        @NotNull
        Long consultorioId,

        @NotNull
        DiaSemana diaSemana,

        @NotNull
        LocalTime horaInicio,

        @NotNull
        LocalTime horaFin
) {

    @AssertTrue(message = "La hora final debe ser posterior a la hora inicial")
    public boolean isHorarioValido() {
        if (horaInicio == null || horaFin == null) {
            return true;
        }

        return horaFin.isAfter(horaInicio);
    }
}