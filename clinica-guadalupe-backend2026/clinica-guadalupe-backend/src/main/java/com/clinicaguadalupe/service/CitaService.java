package com.clinicaguadalupe.service;

import com.clinicaguadalupe.dto.CitaRequest;
import com.clinicaguadalupe.dto.CitaResponse;
import com.clinicaguadalupe.dto.EstadoCitaRequest;

import java.util.List;

public interface CitaService {

    CitaResponse reservar(
            String emailPaciente,
            CitaRequest request
    );

    List<CitaResponse> listarMisCitas(
            String emailPaciente
    );

    CitaResponse cancelar(
            Long id,
            String emailPaciente
    );

    List<CitaResponse> listarTodas();

    CitaResponse cambiarEstado(
            Long id,
            EstadoCitaRequest request
    );

    List<CitaResponse> listarCitasMedico(
            String emailMedico
    );

    CitaResponse cambiarEstadoMedico(
            Long id,
            String emailMedico,
            EstadoCitaRequest request
    );
}