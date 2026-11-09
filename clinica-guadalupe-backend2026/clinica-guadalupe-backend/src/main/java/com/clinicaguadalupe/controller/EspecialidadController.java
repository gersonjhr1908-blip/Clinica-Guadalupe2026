package com.clinicaguadalupe.controller;

import com.clinicaguadalupe.dto.*;
import com.clinicaguadalupe.service.EspecialidadService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/especialidades")
public class EspecialidadController {

    private final EspecialidadService especialidadService;

    public EspecialidadController(
            EspecialidadService especialidadService
    ) {
        this.especialidadService = especialidadService;
    }

    @PostMapping
    public ResponseEntity<EspecialidadResponse> registrar(
            @Valid @RequestBody EspecialidadRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(especialidadService.registrar(request));
    }

    @GetMapping
    public ResponseEntity<List<EspecialidadResponse>>
    listarActivas() {
        return ResponseEntity.ok(
                especialidadService.listarActivas()
        );
    }

    @GetMapping("/todas")
    public ResponseEntity<List<EspecialidadResponse>>
    listarTodas() {
        return ResponseEntity.ok(
                especialidadService.listarTodas()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<EspecialidadResponse> buscarPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                especialidadService.buscarPorId(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<EspecialidadResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody EspecialidadRequest request
    ) {
        return ResponseEntity.ok(
                especialidadService.actualizar(id, request)
        );
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<EspecialidadResponse> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody EstadoRequest request
    ) {
        return ResponseEntity.ok(
                especialidadService.cambiarEstado(
                        id,
                        request.activo()
                )
        );
    }
}