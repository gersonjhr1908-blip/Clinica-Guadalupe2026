package com.clinicaguadalupe.dto;

import com.clinicaguadalupe.entity.EstadoCita;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EstadoCitaRequest(

        @NotNull
        EstadoCita estado,

        @Size(max = 1000)
        String observaciones
) {
}