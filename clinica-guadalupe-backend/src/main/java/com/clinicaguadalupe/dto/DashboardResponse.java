package com.clinicaguadalupe.dto;

import java.math.BigDecimal;

public record DashboardResponse(

        long totalUsuarios,
        long totalPacientes,
        long totalMedicos,
        long totalRecepcionistas,
        long especialidadesActivas,
        long consultoriosActivos,
        long totalCitas,
        long citasHoy,
        long citasPendientes,
        long citasConfirmadas,
        long citasAtendidas,
        long citasCanceladas,
        long pagosAprobados,
        BigDecimal ingresosAprobados
) {
}