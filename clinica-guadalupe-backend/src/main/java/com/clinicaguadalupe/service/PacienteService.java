package com.clinicaguadalupe.service;

import com.clinicaguadalupe.dto.PacienteActualizarRequest;
import com.clinicaguadalupe.dto.PacienteResponse;

import com.clinicaguadalupe.entity.Paciente;

public interface PacienteService {

    Paciente obtenerOCrearPorEmail(String email);

    PacienteResponse obtenerMiPerfil(Long usuarioId);

    PacienteResponse actualizarMiPerfil(
            Long usuarioId,
            PacienteActualizarRequest request
    );
}