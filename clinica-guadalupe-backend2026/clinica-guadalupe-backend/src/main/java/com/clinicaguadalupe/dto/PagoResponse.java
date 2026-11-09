package com.clinicaguadalupe.dto;

import com.clinicaguadalupe.entity.EstadoPago;
import com.clinicaguadalupe.entity.MetodoPago;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagoResponse(

        Long id,
        Long citaId,
        String paciente,
        String medico,
        BigDecimal monto,
        MetodoPago metodoPago,
        EstadoPago estado,
        String codigoOperacion,
        LocalDateTime fechaPago,
        LocalDateTime fechaCreacion
) {
}