package com.clinicaguadalupe.controller;

import com.clinicaguadalupe.dto.PagoRequest;
import com.clinicaguadalupe.dto.PagoResponse;
import com.clinicaguadalupe.service.PagoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @PostMapping
    public ResponseEntity<PagoResponse> procesarPago(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody PagoRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        pagoService.procesarPago(
                                jwt.getSubject(),
                                request
                        )
                );
    }

    @GetMapping("/mis-pagos")
    public ResponseEntity<List<PagoResponse>> listarMisPagos(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(
                pagoService.listarMisPagos(
                        jwt.getSubject()
                )
        );
    }

    @GetMapping("/admin/todos")
    public ResponseEntity<List<PagoResponse>> listarTodos() {
        return ResponseEntity.ok(
                pagoService.listarTodos()
        );
    }

    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<PagoResponse> confirmar(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                pagoService.confirmar(id)
        );
    }

    @PatchMapping("/{id}/reembolsar")
    public ResponseEntity<PagoResponse> reembolsar(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                pagoService.reembolsar(id)
        );
    }
}