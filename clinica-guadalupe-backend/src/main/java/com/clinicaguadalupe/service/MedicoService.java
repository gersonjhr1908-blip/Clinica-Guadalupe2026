package com.clinicaguadalupe.service;

import com.clinicaguadalupe.dto.EstadoRequest;
import com.clinicaguadalupe.dto.MedicoActualizarRequest;
import com.clinicaguadalupe.dto.MedicoPerfilActualizarRequest;
import com.clinicaguadalupe.dto.MedicoRequest;
import com.clinicaguadalupe.dto.MedicoResponse;

import java.util.List;

public interface MedicoService {

    MedicoResponse registrar(MedicoRequest request);

    List<MedicoResponse> listarActivos();

    List<MedicoResponse> listarTodos();

    MedicoResponse buscarPorId(Long id);

    MedicoResponse buscarPorUsuarioId(Long usuarioId);

    MedicoResponse actualizarMiPerfil(Long usuarioId, MedicoPerfilActualizarRequest request);

    MedicoResponse actualizar(
            Long id,
            MedicoActualizarRequest request
    );

    MedicoResponse cambiarEstado(
            Long id,
            EstadoRequest request
    );

    void eliminar(Long id);
}
