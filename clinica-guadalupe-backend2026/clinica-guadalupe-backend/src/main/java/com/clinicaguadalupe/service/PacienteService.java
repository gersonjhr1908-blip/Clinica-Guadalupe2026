package com.clinicaguadalupe.service;

import com.clinicaguadalupe.dto.PacienteActualizarRequest;
import com.clinicaguadalupe.dto.PacienteResponse;

public interface PacienteService {

    PacienteResponse obtenerMiPerfil(Long usuarioId);

    PacienteResponse actualizarMiPerfil(
            Long usuarioId,
            PacienteActualizarRequest request
    );
}