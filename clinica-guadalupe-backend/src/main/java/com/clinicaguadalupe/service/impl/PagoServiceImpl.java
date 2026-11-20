package com.clinicaguadalupe.service.impl;

import com.clinicaguadalupe.dto.PagoRequest;
import com.clinicaguadalupe.dto.PagoResponse;
import com.clinicaguadalupe.entity.*;
import com.clinicaguadalupe.exception.DatoDuplicadoException;
import com.clinicaguadalupe.exception.RecursoNoEncontradoException;
import com.clinicaguadalupe.repository.CitaRepository;
import com.clinicaguadalupe.repository.PagoRepository;
import com.clinicaguadalupe.service.PagoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PagoServiceImpl implements PagoService {

    private final PagoRepository pagoRepository;
    private final CitaRepository citaRepository;

    public PagoServiceImpl(
            PagoRepository pagoRepository,
            CitaRepository citaRepository
    ) {
        this.pagoRepository = pagoRepository;
        this.citaRepository = citaRepository;
    }

    @Override
    @Transactional
    public PagoResponse procesarPago(
            String emailPaciente,
            PagoRequest request
    ) {
        validarMetodoPago(request);
        Cita cita = citaRepository
                .findByIdAndPacienteUsuarioEmail(
                        request.citaId(),
                        emailPaciente
                )
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "La cita no pertenece al paciente autenticado"
                        )
                );

        if (cita.getEstado() != EstadoCita.CONFIRMADA) {
            throw new DatoDuplicadoException(
                    "Solo se pueden pagar citas confirmadas"
            );
        }

        if (pagoRepository.existsByCitaId(cita.getId())) {
            throw new DatoDuplicadoException(
                    "La cita ya tiene un pago registrado"
            );
        }

        Pago pago = new Pago();
        pago.setCita(cita);
        pago.setMonto(
                cita.getMedico().getPrecioConsulta()
        );
        pago.setMetodoPago(request.metodoPago());

        if (request.metodoPago() == MetodoPago.EFECTIVO) {
            pago.setEstado(EstadoPago.PENDIENTE);
        } else {
            pago.setEstado(EstadoPago.APROBADO);
            pago.setCodigoOperacion(generarCodigoOperacion());
            pago.setFechaPago(LocalDateTime.now());
        }

        return convertirRespuesta(
                pagoRepository.save(pago)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponse> listarMisPagos(
            String emailPaciente
    ) {
        return pagoRepository
                .findByCitaPacienteUsuarioEmailOrderByFechaCreacionDesc(
                        emailPaciente
                )
                .stream()
                .map(this::convertirRespuesta)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponse> listarTodos() {
        return pagoRepository
                .findAllByOrderByFechaCreacionDesc()
                .stream()
                .map(this::convertirRespuesta)
                .toList();
    }

    @Override
    @Transactional
    public PagoResponse confirmar(Long id) {
        Pago pago = obtenerPago(id);

        if (pago.getEstado() != EstadoPago.PENDIENTE) {
            throw new DatoDuplicadoException(
                    "El pago no se encuentra pendiente"
            );
        }

        pago.setEstado(EstadoPago.APROBADO);
        pago.setCodigoOperacion(generarCodigoOperacion());
        pago.setFechaPago(LocalDateTime.now());

        return convertirRespuesta(
                pagoRepository.save(pago)
        );
    }

    @Override
    @Transactional
    public PagoResponse reembolsar(Long id) {
        Pago pago = obtenerPago(id);

        if (pago.getEstado() != EstadoPago.APROBADO) {
            throw new DatoDuplicadoException(
                    "Solo se pueden reembolsar pagos aprobados"
            );
        }

        pago.setEstado(EstadoPago.REEMBOLSADO);

        return convertirRespuesta(
                pagoRepository.save(pago)
        );
    }

    private Pago obtenerPago(Long id) {
        return pagoRepository
                .findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Pago no encontrado"
                        )
                );
    }

    private String generarCodigoOperacion() {
        return "CG-"
                + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase();
    }

    private PagoResponse convertirRespuesta(Pago pago) {
        Usuario paciente =
                pago.getCita().getPaciente().getUsuario();

        Usuario medico =
                pago.getCita().getMedico().getUsuario();

        return new PagoResponse(
                pago.getId(),
                pago.getCita().getId(),
                paciente.getNombres()
                        + " "
                        + paciente.getApellidos(),
                medico.getNombres()
                        + " "
                        + medico.getApellidos(),
                pago.getMonto(),
                pago.getMetodoPago(),
                pago.getEstado(),
                pago.getCodigoOperacion(),
                pago.getFechaPago(),
                pago.getFechaCreacion()
        );
    }
    private void validarMetodoPago(PagoRequest request) {
        if (request.metodoPago() == null) {
            throw new IllegalArgumentException("El método de pago es obligatorio");
        }
    }
}