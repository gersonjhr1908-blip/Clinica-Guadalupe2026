package com.clinicaguadalupe.controller;

import com.clinicaguadalupe.dto.UsuarioRequest;
import com.clinicaguadalupe.dto.UsuarioResponse;
import com.clinicaguadalupe.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.clinicaguadalupe.dto.EstadoUsuarioRequest;
import com.clinicaguadalupe.dto.UsuarioActualizarRequest;
import com.clinicaguadalupe.dto.CambiarPasswordRequest;

import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> registrar(
            @Valid @RequestBody UsuarioRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(usuarioService.registrar(request));
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listar() {
        return ResponseEntity.ok(usuarioService.listar());
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> miPerfil(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(usuarioService.buscarPorEmail(jwt.getSubject()));
    }

    @PutMapping("/me")
    public ResponseEntity<UsuarioResponse> actualizarMiPerfil(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UsuarioActualizarRequest request
    ) {
        UsuarioResponse actual = usuarioService.buscarPorEmail(jwt.getSubject());
        return ResponseEntity.ok(usuarioService.actualizar(actual.id(), request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> buscarPorId(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<UsuarioResponse> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody EstadoUsuarioRequest request
    ) {
        return ResponseEntity.ok(
                usuarioService.cambiarEstado(id, request.activo())
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioActualizarRequest request
    ) {
        return ResponseEntity.ok(
                usuarioService.actualizar(id, request)
        );
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> cambiarPassword(
            @PathVariable Long id,
            @Valid @RequestBody CambiarPasswordRequest request
    ) {
        usuarioService.cambiarPassword(id, request);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
