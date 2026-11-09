package com.clinicaguadalupe.controller;

import com.clinicaguadalupe.dto.EstadoRequest;
import com.clinicaguadalupe.dto.HorarioMedicoRequest;
import com.clinicaguadalupe.dto.HorarioMedicoResponse;
import com.clinicaguadalupe.service.HorarioMedicoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

@RestController
@RequestMapping("/api/horarios")
public class HorarioMedicoController {

    private final HorarioMedicoService horarioService;

    public HorarioMedicoController(
            HorarioMedicoService horarioService
    ) {
        this.horarioService = horarioService;
    }

    @PostMapping
    public ResponseEntity<HorarioMedicoResponse> registrar(
            @Valid @RequestBody HorarioMedicoRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(horarioService.registrar(request));
    }

    @PostMapping("/me")
    public ResponseEntity<HorarioMedicoResponse> registrarPropio(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody HorarioMedicoRequest request
    ) {
        Long usuarioId = Long.valueOf(String.valueOf(jwt.getClaims().get("usuarioId")));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(horarioService.registrarPropio(usuarioId, request));
    }

    @GetMapping("/medico/{medicoId}")
    public ResponseEntity<List<HorarioMedicoResponse>> listarPorMedico(
            @PathVariable Long medicoId
    ) {
        return ResponseEntity.ok(
                horarioService.listarPorMedico(medicoId)
        );
    }

    @GetMapping("/admin/todos")
    public ResponseEntity<List<HorarioMedicoResponse>> listarTodos() {
        return ResponseEntity.ok(
                horarioService.listarTodos()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<HorarioMedicoResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody HorarioMedicoRequest request
    ) {
        return ResponseEntity.ok(
                horarioService.actualizar(id, request)
        );
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<HorarioMedicoResponse> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody EstadoRequest request
    ) {
        return ResponseEntity.ok(
                horarioService.cambiarEstado(id, request)
        );
    }
}
