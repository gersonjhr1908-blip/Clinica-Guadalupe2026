package com.clinicaguadalupe.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public record CitaRequest(

        @NotNull
        Long medicoId,

        @NotNull
        @FutureOrPresent(message = "La fecha no puede estar en el pasado")
        LocalDate fecha,

        @NotNull
        LocalTime hora,

        @NotBlank
        @Size(max = 500)
        String motivo
) {
}