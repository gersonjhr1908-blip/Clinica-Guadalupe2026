package com.clinicaguadalupe.service;

import com.clinicaguadalupe.dto.ConsultorioRequest;
import com.clinicaguadalupe.dto.ConsultorioResponse;
import com.clinicaguadalupe.dto.EstadoRequest;

import java.util.List;

public interface ConsultorioService {

    ConsultorioResponse registrar(ConsultorioRequest request);

    List<ConsultorioResponse> listarActivos();

    List<ConsultorioResponse> listarTodos();

    ConsultorioResponse buscarPorId(Long id);

    ConsultorioResponse actualizar(
            Long id,
            ConsultorioRequest request
    );

    ConsultorioResponse cambiarEstado(
            Long id,
            EstadoRequest request
    );
}