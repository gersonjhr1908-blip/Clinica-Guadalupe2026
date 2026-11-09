package com.clinicaguadalupe.controller;

import com.clinicaguadalupe.dto.EstadoRequest;
import com.clinicaguadalupe.dto.MedicoActualizarRequest;
import com.clinicaguadalupe.dto.MedicoRequest;
import com.clinicaguadalupe.dto.MedicoResponse;
import com.clinicaguadalupe.service.MedicoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

@RestController
@RequestMapping("/api/medicos")
public class MedicoController {

    private final MedicoService medicoService;

    public MedicoController(MedicoService medicoService) {
        this.medicoService = medicoService;
    }

    @PostMapping
    public ResponseEntity<MedicoResponse> registrar(
            @Valid @RequestBody MedicoRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(medicoService.registrar(request));
    }

    @GetMapping
    public ResponseEntity<List<MedicoResponse>> listarActivos() {
        return ResponseEntity.ok(
                medicoService.listarActivos()
        );
    }

    @GetMapping("/admin/todos")
    public ResponseEntity<List<MedicoResponse>> listarTodos() {
        return ResponseEntity.ok(
                medicoService.listarTodos()
        );
    }

    @GetMapping("/me")
    public ResponseEntity<MedicoResponse> miPerfil(@AuthenticationPrincipal Jwt jwt) {
        Object valor = jwt.getClaims().get("usuarioId");
        Long usuarioId = Long.valueOf(String.valueOf(valor));
        return ResponseEntity.ok(medicoService.buscarPorUsuarioId(usuarioId));
    }

    @PutMapping("/me")
    public ResponseEntity<MedicoResponse> actualizarMiPerfil(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody com.clinicaguadalupe.dto.MedicoPerfilActualizarRequest request
    ) {
        Object valor = jwt.getClaims().get("usuarioId");
        Long usuarioId = Long.valueOf(String.valueOf(valor));
        return ResponseEntity.ok(medicoService.actualizarMiPerfil(usuarioId, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedicoResponse> buscarPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                medicoService.buscarPorId(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<MedicoResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody MedicoActualizarRequest request
    ) {
        return ResponseEntity.ok(
                medicoService.actualizar(id, request)
        );
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<MedicoResponse> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody EstadoRequest request
    ) {
        return ResponseEntity.ok(
                medicoService.cambiarEstado(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        medicoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
