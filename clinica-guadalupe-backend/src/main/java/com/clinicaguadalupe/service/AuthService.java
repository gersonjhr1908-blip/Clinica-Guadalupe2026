package com.clinicaguadalupe.service;

import com.clinicaguadalupe.dto.LoginRequest;
import com.clinicaguadalupe.dto.LoginResponse;
import com.clinicaguadalupe.dto.RegistroPacienteRequest;
import com.clinicaguadalupe.dto.UsuarioResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    UsuarioResponse registrarPaciente(
            RegistroPacienteRequest request
    );
}