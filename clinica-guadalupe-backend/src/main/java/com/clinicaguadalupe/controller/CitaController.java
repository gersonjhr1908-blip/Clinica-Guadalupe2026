package com.clinicaguadalupe.controller;

import com.clinicaguadalupe.dto.CitaRequest;
import com.clinicaguadalupe.dto.CitaResponse;
import com.clinicaguadalupe.dto.EstadoCitaRequest;
import com.clinicaguadalupe.service.CitaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    private final CitaService citaService;

    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    @PostMapping
    public ResponseEntity<CitaResponse> reservar(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CitaRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        citaService.reservar(
                                jwt.getSubject(),
                                request
                        )
                );
    }

    @GetMapping("/mis-citas")
    public ResponseEntity<List<CitaResponse>> listarMisCitas(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(
                citaService.listarMisCitas(
                        jwt.getSubject()
                )
        );
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<CitaResponse> cancelar(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(
                citaService.cancelar(
                        id,
                        jwt.getSubject()
                )
        );
    }

    @GetMapping("/admin/todas")
    public ResponseEntity<List<CitaResponse>> listarTodas() {
        return ResponseEntity.ok(
                citaService.listarTodas()
        );
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<CitaResponse> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody EstadoCitaRequest request
    ) {
        return ResponseEntity.ok(
                citaService.cambiarEstado(id, request)
        );
    }

    @GetMapping("/medico/mis-citas")
    public ResponseEntity<List<CitaResponse>> listarCitasMedico(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(
                citaService.listarCitasMedico(
                        jwt.getSubject()
                )
        );
    }

    @PatchMapping("/medico/{id}/estado")
    public ResponseEntity<CitaResponse> cambiarEstadoMedico(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody EstadoCitaRequest request
    ) {
        return ResponseEntity.ok(
                citaService.cambiarEstadoMedico(
                        id,
                        jwt.getSubject(),
                        request
                )
        );
    }
}