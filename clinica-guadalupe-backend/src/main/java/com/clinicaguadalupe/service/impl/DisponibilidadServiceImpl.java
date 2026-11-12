package com.clinicaguadalupe.service.impl;

import com.clinicaguadalupe.dto.DisponibilidadResponse;
import com.clinicaguadalupe.entity.*;
import com.clinicaguadalupe.exception.RecursoNoEncontradoException;
import com.clinicaguadalupe.repository.CitaRepository;
import com.clinicaguadalupe.repository.HorarioMedicoRepository;
import com.clinicaguadalupe.repository.MedicoRepository;
import com.clinicaguadalupe.service.DisponibilidadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DisponibilidadServiceImpl
        implements DisponibilidadService {

    private final MedicoRepository medicoRepository;
    private final HorarioMedicoRepository horarioRepository;
    private final CitaRepository citaRepository;

    public DisponibilidadServiceImpl(
            MedicoRepository medicoRepository,
            HorarioMedicoRepository horarioRepository,
            CitaRepository citaRepository
    ) {
        this.medicoRepository = medicoRepository;
        this.horarioRepository = horarioRepository;
        this.citaRepository = citaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DisponibilidadResponse> obtenerDisponibilidad(
            Long medicoId,
            LocalDate fecha
    ) {
        Medico medico = medicoRepository
                .findById(medicoId)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Médico no encontrado"
                        )
                );

        if (Boolean.FALSE.equals(medico.getActivo())) {
            throw new RecursoNoEncontradoException(
                    "El médico se encuentra desactivado"
            );
        }

        if (fecha.isBefore(LocalDate.now())) {
            return List.of();
        }

        DiaSemana diaSemana = convertirDia(
                fecha.getDayOfWeek()
        );

        List<HorarioMedico> horarios = horarioRepository
                .findByMedicoIdAndDiaSemanaAndActivoTrueOrderByHoraInicioAsc(
                        medicoId,
                        diaSemana
                );

        LocalDateTime inicioDia = fecha.atStartOfDay();
        LocalDateTime finDia = inicioDia.plusDays(1);

        List<Cita> citasOcupadas = citaRepository
                .buscarCitasActivasDelMedico(
                        medicoId,
                        inicioDia,
                        finDia,
                        EstadoCita.CANCELADA
                );

        List<DisponibilidadResponse> disponibles =
                new ArrayList<>();

        int duracion = medico.getDuracionConsultaMinutos();

        for (HorarioMedico horario : horarios) {
            LocalDateTime horaActual = fecha.atTime(horario.getHoraInicio());
            LocalDateTime finHorario = fecha.atTime(horario.getHoraFin());

            while (!horaActual
                    .plusMinutes(duracion)
                    .isAfter(finHorario)) {

                LocalDateTime inicioTurno =
                        horaActual;

                LocalDateTime finTurno =
                        inicioTurno.plusMinutes(duracion);

                boolean esFuturo =
                        inicioTurno.isAfter(LocalDateTime.now());

                boolean ocupado = citasOcupadas
                        .stream()
                        .anyMatch(cita ->
                                cita.getFechaHoraInicio()
                                        .isBefore(finTurno)
                                        &&
                                        cita.getFechaHoraFin()
                                                .isAfter(inicioTurno)
                        );

                if (esFuturo && !ocupado) {
                    disponibles.add(
                            new DisponibilidadResponse(
                                    horario.getId(),
                                    horario.getConsultorio().getId(),
                                    horario.getConsultorio().getCodigo()
                                            + " - "
                                            + horario.getConsultorio().getNombre(),
                                    fecha,
                                    inicioTurno.toLocalTime(),
                                    finTurno.toLocalTime()
                            )
                    );
                }

                horaActual = horaActual.plusMinutes(duracion);
            }
        }

        return disponibles;
    }

    private DiaSemana convertirDia(DayOfWeek dia) {
        return switch (dia) {
            case MONDAY -> DiaSemana.LUNES;
            case TUESDAY -> DiaSemana.MARTES;
            case WEDNESDAY -> DiaSemana.MIERCOLES;
            case THURSDAY -> DiaSemana.JUEVES;
            case FRIDAY -> DiaSemana.VIERNES;
            case SATURDAY -> DiaSemana.SABADO;
            case SUNDAY -> DiaSemana.DOMINGO;
        };
    }
}
