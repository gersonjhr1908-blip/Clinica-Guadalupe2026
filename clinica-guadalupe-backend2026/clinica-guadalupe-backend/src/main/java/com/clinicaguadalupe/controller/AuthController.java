package com.clinicaguadalupe.controller;

import com.clinicaguadalupe.dto.LoginRequest;
import com.clinicaguadalupe.dto.LoginResponse;
import com.clinicaguadalupe.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.clinicaguadalupe.dto.RegistroPacienteRequest;
import com.clinicaguadalupe.dto.UsuarioResponse;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/registro-paciente")
    public ResponseEntity<UsuarioResponse> registrarPaciente(
            @Valid @RequestBody RegistroPacienteRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.registrarPaciente(request));
    }
}