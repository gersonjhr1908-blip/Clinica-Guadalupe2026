package com.clinicaguadalupe.controller;

import com.clinicaguadalupe.dto.PacienteActualizarRequest;
import com.clinicaguadalupe.dto.PacienteResponse;
import com.clinicaguadalupe.service.PacienteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

    private final PacienteService pacienteService;

    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    @GetMapping("/me")
    public PacienteResponse obtenerMiPerfil(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return pacienteService.obtenerMiPerfil(obtenerUsuarioId(jwt));
    }

    @PutMapping("/me")
    public PacienteResponse actualizarMiPerfil(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody PacienteActualizarRequest request
    ) {
        return pacienteService.actualizarMiPerfil(
                obtenerUsuarioId(jwt),
                request
        );
    }

    private Long obtenerUsuarioId(Jwt jwt) {
        Object valor = jwt.getClaims().get("usuarioId");

        try {
            Long usuarioId = Long.valueOf(String.valueOf(valor));

            if (usuarioId <= 0) {
                throw new NumberFormatException();
            }

            return usuarioId;
        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "El token no contiene un usuarioId válido"
            );
        }
    }
}