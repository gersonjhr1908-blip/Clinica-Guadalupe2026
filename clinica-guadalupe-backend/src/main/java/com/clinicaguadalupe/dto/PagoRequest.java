package com.clinicaguadalupe.dto;

import com.clinicaguadalupe.entity.MetodoPago;
import jakarta.validation.constraints.NotNull;

public record PagoRequest(

        @NotNull
        Long citaId,

        @NotNull
        MetodoPago metodoPago
) {
}