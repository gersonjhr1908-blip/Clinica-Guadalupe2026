package com.clinicaguadalupe.service;

import com.clinicaguadalupe.dto.PagoRequest;
import com.clinicaguadalupe.dto.PagoResponse;

import java.util.List;

public interface PagoService {

    PagoResponse procesarPago(
            String emailPaciente,
            PagoRequest request
    );

    List<PagoResponse> listarMisPagos(
            String emailPaciente
    );

    List<PagoResponse> listarTodos();

    PagoResponse confirmar(Long id);

    PagoResponse reembolsar(Long id);
}