package com.clinicaguadalupe.controller;

import com.clinicaguadalupe.dto.ConsultorioRequest;
import com.clinicaguadalupe.dto.ConsultorioResponse;
import com.clinicaguadalupe.dto.EstadoRequest;
import com.clinicaguadalupe.service.ConsultorioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consultorios")
public class ConsultorioController {

    private final ConsultorioService consultorioService;

    public ConsultorioController(
            ConsultorioService consultorioService
    ) {
        this.consultorioService = consultorioService;
    }

    @PostMapping
    public ResponseEntity<ConsultorioResponse> registrar(
            @Valid @RequestBody ConsultorioRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(consultorioService.registrar(request));
    }

    @GetMapping
    public ResponseEntity<List<ConsultorioResponse>> listarActivos() {
        return ResponseEntity.ok(
                consultorioService.listarActivos()
        );
    }

    @GetMapping("/admin/todos")
    public ResponseEntity<List<ConsultorioResponse>> listarTodos() {
        return ResponseEntity.ok(
                consultorioService.listarTodos()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConsultorioResponse> buscarPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                consultorioService.buscarPorId(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConsultorioResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ConsultorioRequest request
    ) {
        return ResponseEntity.ok(
                consultorioService.actualizar(id, request)
        );
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ConsultorioResponse> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody EstadoRequest request
    ) {
        return ResponseEntity.ok(
                consultorioService.cambiarEstado(id, request)
        );
    }
}