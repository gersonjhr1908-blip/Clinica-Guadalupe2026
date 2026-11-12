package com.clinicaguadalupe.service;

import com.clinicaguadalupe.dto.EspecialidadRequest;
import com.clinicaguadalupe.dto.EspecialidadResponse;

import java.util.List;

public interface EspecialidadService {

    EspecialidadResponse registrar(
            EspecialidadRequest request
    );

    List<EspecialidadResponse> listarActivas();

    List<EspecialidadResponse> listarTodas();

    EspecialidadResponse buscarPorId(Long id);

    EspecialidadResponse actualizar(
            Long id,
            EspecialidadRequest request
    );

    EspecialidadResponse cambiarEstado(
            Long id,
            Boolean activo
    );
}