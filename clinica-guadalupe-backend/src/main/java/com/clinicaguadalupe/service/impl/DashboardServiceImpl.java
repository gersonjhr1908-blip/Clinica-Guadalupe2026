package com.clinicaguadalupe.service.impl;

import com.clinicaguadalupe.dto.DashboardResponse;
import com.clinicaguadalupe.entity.EstadoCita;
import com.clinicaguadalupe.entity.EstadoPago;
import com.clinicaguadalupe.entity.Rol;
import com.clinicaguadalupe.repository.*;
import com.clinicaguadalupe.service.DashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final UsuarioRepository usuarioRepository;
    private final EspecialidadRepository especialidadRepository;
    private final MedicoRepository medicoRepository;
    private final ConsultorioRepository consultorioRepository;
    private final CitaRepository citaRepository;
    private final PagoRepository pagoRepository;

    public DashboardServiceImpl(
            UsuarioRepository usuarioRepository,
            EspecialidadRepository especialidadRepository,
            MedicoRepository medicoRepository,
            ConsultorioRepository consultorioRepository,
            CitaRepository citaRepository,
            PagoRepository pagoRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.especialidadRepository = especialidadRepository;
        this.medicoRepository = medicoRepository;
        this.consultorioRepository = consultorioRepository;
        this.citaRepository = citaRepository;
        this.pagoRepository = pagoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse obtenerResumen() {

        LocalDateTime inicioHoy =
                LocalDate.now().atStartOfDay();

        LocalDateTime finHoy =
                inicioHoy.plusDays(1);

        return new DashboardResponse(
                usuarioRepository.count(),
                usuarioRepository.countByRol(Rol.PACIENTE),
                medicoRepository.countByActivoTrue(),
                usuarioRepository.countByRol(Rol.RECEPCIONISTA),
                especialidadRepository.countByActivoTrue(),
                consultorioRepository.countByActivoTrue(),
                citaRepository.count(),
                citaRepository
                        .countByFechaHoraInicioGreaterThanEqualAndFechaHoraInicioLessThan(
                                inicioHoy,
                                finHoy
                        ),
                citaRepository.countByEstado(
                        EstadoCita.PENDIENTE
                ),
                citaRepository.countByEstado(
                        EstadoCita.CONFIRMADA
                ),
                citaRepository.countByEstado(
                        EstadoCita.ATENDIDA
                ),
                citaRepository.countByEstado(
                        EstadoCita.CANCELADA
                ),
                pagoRepository.countByEstado(
                        EstadoPago.APROBADO
                ),
                pagoRepository.sumarMontoPorEstado(
                        EstadoPago.APROBADO
                )
        );
    }
}