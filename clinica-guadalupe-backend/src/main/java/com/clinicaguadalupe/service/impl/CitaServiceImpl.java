package com.clinicaguadalupe.service.impl;

import com.clinicaguadalupe.dto.CitaRequest;
import com.clinicaguadalupe.dto.CitaResponse;
import com.clinicaguadalupe.dto.EstadoCitaRequest;
import com.clinicaguadalupe.entity.*;
import com.clinicaguadalupe.exception.DatoDuplicadoException;
import com.clinicaguadalupe.exception.RecursoNoEncontradoException;
import com.clinicaguadalupe.repository.CitaRepository;
import com.clinicaguadalupe.repository.HorarioMedicoRepository;
import com.clinicaguadalupe.repository.MedicoRepository;
import com.clinicaguadalupe.service.PacienteService;
import com.clinicaguadalupe.service.CitaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.text.Normalizer;
import java.util.List;

@Service
public class CitaServiceImpl implements CitaService {

    private final CitaRepository citaRepository;
    private final PacienteService pacienteService;
    private final MedicoRepository medicoRepository;
    private final HorarioMedicoRepository horarioRepository;

    public CitaServiceImpl(
            CitaRepository citaRepository,
            PacienteService pacienteService,
            MedicoRepository medicoRepository,
            HorarioMedicoRepository horarioRepository
    ) {
        this.citaRepository = citaRepository;
        this.pacienteService = pacienteService;
        this.medicoRepository = medicoRepository;
        this.horarioRepository = horarioRepository;
    }

    @Override
    @Transactional
    public CitaResponse reservar(
            String emailPaciente,
            CitaRequest request
    ) {
        Paciente paciente = pacienteService.obtenerOCrearPorEmail(emailPaciente);
        Medico medico = obtenerMedico(request.medicoId());

        LocalDateTime inicio = LocalDateTime.of(
                request.fecha(),
                request.hora()
        );

        if (!inicio.isAfter(LocalDateTime.now())) {
            throw new DatoDuplicadoException(
                    "La cita debe reservarse para una fecha y hora futura"
            );
        }

        LocalDateTime fin = inicio.plusMinutes(
                medico.getDuracionConsultaMinutos()
        );

        DiaSemana diaSemana = convertirDia(
                request.fecha().getDayOfWeek()
        );

        List<HorarioMedico> horarios = horarioRepository
                .findByMedicoIdAndDiaSemanaAndActivoTrueOrderByHoraInicioAsc(
                        medico.getId(),
                        diaSemana
                );

        HorarioMedico horarioSeleccionado = horarios
                .stream()
                .filter(horario ->
                        !request.hora().isBefore(
                                horario.getHoraInicio()
                        )
                                && !fin.toLocalTime().isAfter(
                                horario.getHoraFin()
                        )
                                && fin.toLocalDate().equals(
                                request.fecha()
                        )
                )
                .findFirst()
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "El médico no atiende en la fecha y hora seleccionada"
                        )
                );

        validarDisponibilidad(
                paciente,
                medico,
                horarioSeleccionado.getConsultorio(),
                inicio,
                fin
        );

        Cita cita = new Cita();
        cita.setPaciente(paciente);
        cita.setMedico(medico);
        cita.setConsultorio(
                horarioSeleccionado.getConsultorio()
        );
        cita.setHorarioMedico(horarioSeleccionado);
        cita.setFechaHoraInicio(inicio);
        cita.setFechaHoraFin(fin);
        cita.setMotivo(request.motivo().trim());
        cita.setEstado(EstadoCita.PENDIENTE);

        return convertirRespuesta(
                citaRepository.save(cita)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<CitaResponse> listarMisCitas(
            String emailPaciente
    ) {
        return citaRepository
                .findByPacienteUsuarioEmailOrderByFechaHoraInicioDesc(
                        emailPaciente
                )
                .stream()
                .map(this::convertirRespuesta)
                .toList();
    }

    @Override
    @Transactional
    public CitaResponse cancelar(
            Long id,
            String emailPaciente
    ) {
        Cita cita = citaRepository
                .findByIdAndPacienteUsuarioEmail(
                        id,
                        emailPaciente
                )
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Cita no encontrada"
                        )
                );

        if (cita.getEstado() == EstadoCita.ATENDIDA
                || cita.getEstado() == EstadoCita.CANCELADA
                || cita.getEstado() == EstadoCita.NO_ASISTIO) {
            throw new DatoDuplicadoException(
                    "La cita ya no puede cancelarse"
            );
        }

        if (!cita.getFechaHoraInicio().isAfter(
                LocalDateTime.now()
        )) {
            throw new DatoDuplicadoException(
                    "No se puede cancelar una cita pasada"
            );
        }

        cita.setEstado(EstadoCita.CANCELADA);

        return convertirRespuesta(
                citaRepository.save(cita)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<CitaResponse> listarTodas() {
        return citaRepository
                .findAllByOrderByFechaHoraInicioDesc()
                .stream()
                .map(this::convertirRespuesta)
                .toList();
    }

    @Override
    @Transactional
    public CitaResponse cambiarEstado(
            Long id,
            EstadoCitaRequest request
    ) {
        Cita cita = citaRepository
                .findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Cita no encontrada"
                        )
                );

        cita.setEstado(request.estado());
        cita.setObservaciones(request.observaciones());

        return convertirRespuesta(
                citaRepository.save(cita)
        );
    }

    private void validarDisponibilidad(
            Paciente paciente,
            Medico medico,
            Consultorio consultorio,
            LocalDateTime inicio,
            LocalDateTime fin
    ) {
        if (citaRepository.contarCrucesMedico(
                medico.getId(),
                inicio,
                fin,
                EstadoCita.CANCELADA
        ) > 0) {
            throw new DatoDuplicadoException(
                    "El médico ya tiene una cita en ese horario"
            );
        }

        if (citaRepository.contarCrucesPaciente(
                paciente.getId(),
                inicio,
                fin,
                EstadoCita.CANCELADA
        ) > 0) {
            throw new DatoDuplicadoException(
                    "El paciente ya tiene una cita en ese horario"
            );
        }

        if (citaRepository.contarCrucesConsultorio(
                consultorio.getId(),
                inicio,
                fin,
                EstadoCita.CANCELADA
        ) > 0) {
            throw new DatoDuplicadoException(
                    "El consultorio está ocupado en ese horario"
            );
        }
    }

    private Medico obtenerMedico(Long id) {
        Medico medico = medicoRepository
                .findById(id)
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

        return medico;
    }

    private DiaSemana convertirDia(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> DiaSemana.LUNES;
            case TUESDAY -> DiaSemana.MARTES;
            case WEDNESDAY -> DiaSemana.MIERCOLES;
            case THURSDAY -> DiaSemana.JUEVES;
            case FRIDAY -> DiaSemana.VIERNES;
            case SATURDAY -> DiaSemana.SABADO;
            case SUNDAY -> DiaSemana.DOMINGO;
        };
    }

    private CitaResponse convertirRespuesta(Cita cita) {
        Usuario usuarioPaciente =
                cita.getPaciente().getUsuario();

        Usuario usuarioMedico =
                cita.getMedico().getUsuario();

        return new CitaResponse(
                cita.getId(),
                generarCodigo(cita),
                cita.getPaciente().getId(),
                usuarioPaciente.getNombres()
                        + " "
                        + usuarioPaciente.getApellidos(),
                cita.getMedico().getId(),
                usuarioMedico.getNombres()
                        + " "
                        + usuarioMedico.getApellidos(),
                cita.getMedico()
                        .getEspecialidad()
                        .getNombre(),
                cita.getConsultorio().getId(),
                cita.getConsultorio().getCodigo()
                        + " - "
                        + cita.getConsultorio().getNombre(),
                cita.getFechaHoraInicio(),
                cita.getFechaHoraFin(),
                cita.getMotivo(),
                cita.getObservaciones(),
                cita.getEstado(),
                cita.getFechaCreacion(),
                cita.getFechaActualizacion()
        );
    }

    private String generarCodigo(Cita cita) {
        String nombre = Normalizer.normalize(cita.getMedico().getEspecialidad().getNombre(), Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "").replaceAll("[^A-Za-z]", "").toUpperCase();
        String prefijo = (nombre + "XXX").substring(0, 3);
        return prefijo + String.format("%05d", cita.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CitaResponse> listarCitasMedico(
            String emailMedico
    ) {
        return citaRepository
                .findByMedicoUsuarioEmailOrderByFechaHoraInicioAsc(
                        emailMedico
                )
                .stream()
                .map(this::convertirRespuesta)
                .toList();
    }

    @Override
    @Transactional
    public CitaResponse cambiarEstadoMedico(
            Long id,
            String emailMedico,
            EstadoCitaRequest request
    ) {
        Cita cita = citaRepository
                .findByIdAndMedicoUsuarioEmail(
                        id,
                        emailMedico
                )
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "La cita no pertenece al médico autenticado"
                        )
                );

        if (request.estado() != EstadoCita.ATENDIDA
                && request.estado() != EstadoCita.NO_ASISTIO) {
            throw new DatoDuplicadoException(
                    "El médico solo puede marcar la cita como ATENDIDA o NO_ASISTIO"
            );
        }

        if (cita.getEstado() == EstadoCita.CANCELADA) {
            throw new DatoDuplicadoException(
                    "Una cita cancelada no puede ser modificada"
            );
        }

        if (LocalDateTime.now().isBefore(
                cita.getFechaHoraInicio()
        )) {
            throw new DatoDuplicadoException(
                    "La cita todavía no ha comenzado"
            );
        }

        cita.setEstado(request.estado());
        cita.setObservaciones(request.observaciones());

        return convertirRespuesta(
                citaRepository.save(cita)
        );
    }
}
