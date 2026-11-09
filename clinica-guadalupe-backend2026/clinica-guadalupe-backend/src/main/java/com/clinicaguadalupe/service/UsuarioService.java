package com.clinicaguadalupe.service;

import com.clinicaguadalupe.dto.UsuarioRequest;
import com.clinicaguadalupe.dto.UsuarioResponse;
import com.clinicaguadalupe.dto.UsuarioActualizarRequest;
import com.clinicaguadalupe.dto.CambiarPasswordRequest;

import java.util.List;

public interface UsuarioService {

    UsuarioResponse registrar(UsuarioRequest request);

    List<UsuarioResponse> listar();

    UsuarioResponse buscarPorId(Long id);
    UsuarioResponse buscarPorEmail(String email);

    UsuarioResponse cambiarEstado(Long id, Boolean activo);

    UsuarioResponse actualizar(Long id, UsuarioActualizarRequest request);

    void eliminar(Long id);

    void cambiarPassword(Long id, CambiarPasswordRequest request);
}
