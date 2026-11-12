package com.clinicaguadalupe.service;

import com.clinicaguadalupe.dto.DisponibilidadResponse;

import java.time.LocalDate;
import java.util.List;

public interface DisponibilidadService {

    List<DisponibilidadResponse> obtenerDisponibilidad(
            Long medicoId,
            LocalDate fecha
    );
}