package com.clinicaguadalupe.controller;

import com.clinicaguadalupe.dto.DashboardResponse;
import com.clinicaguadalupe.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(
            DashboardService dashboardService
    ) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/resumen")
    public ResponseEntity<DashboardResponse> obtenerResumen() {
        return ResponseEntity.ok(
                dashboardService.obtenerResumen()
        );
    }
}