package com.clinicaguadalupe.controller;

import com.clinicaguadalupe.dto.DisponibilidadResponse;
import com.clinicaguadalupe.service.DisponibilidadService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/disponibilidad")
public class DisponibilidadController {

    private final DisponibilidadService disponibilidadService;

    public DisponibilidadController(
            DisponibilidadService disponibilidadService
    ) {
        this.disponibilidadService = disponibilidadService;
    }

    @GetMapping("/medicos/{medicoId}")
    public ResponseEntity<List<DisponibilidadResponse>>
    obtenerDisponibilidad(
            @PathVariable Long medicoId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha
    ) {
        return ResponseEntity.ok(
                disponibilidadService.obtenerDisponibilidad(
                        medicoId,
                        fecha
                )
        );
    }
}