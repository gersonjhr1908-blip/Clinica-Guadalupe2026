package com.clinicaguadalupe.service;

import com.clinicaguadalupe.dto.EstadoRequest;
import com.clinicaguadalupe.dto.HorarioMedicoRequest;
import com.clinicaguadalupe.dto.HorarioMedicoResponse;

import java.util.List;

public interface HorarioMedicoService {

    HorarioMedicoResponse registrar(
            HorarioMedicoRequest request
    );

    HorarioMedicoResponse registrarPropio(Long usuarioId, HorarioMedicoRequest request);

    List<HorarioMedicoResponse> listarPorMedico(
            Long medicoId
    );

    List<HorarioMedicoResponse> listarTodos();

    HorarioMedicoResponse actualizar(
            Long id,
            HorarioMedicoRequest request
    );

    HorarioMedicoResponse cambiarEstado(
            Long id,
            EstadoRequest request
    );
}
