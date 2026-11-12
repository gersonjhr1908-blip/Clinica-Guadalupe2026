package com.clinicaguadalupe.service.impl;

import com.clinicaguadalupe.dto.EstadoRequest;
import com.clinicaguadalupe.dto.HorarioMedicoRequest;
import com.clinicaguadalupe.dto.HorarioMedicoResponse;
import com.clinicaguadalupe.entity.Consultorio;
import com.clinicaguadalupe.entity.HorarioMedico;
import com.clinicaguadalupe.entity.Medico;
import com.clinicaguadalupe.exception.DatoDuplicadoException;
import com.clinicaguadalupe.exception.RecursoNoEncontradoException;
import com.clinicaguadalupe.repository.ConsultorioRepository;
import com.clinicaguadalupe.repository.HorarioMedicoRepository;
import com.clinicaguadalupe.repository.MedicoRepository;
import com.clinicaguadalupe.service.HorarioMedicoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HorarioMedicoServiceImpl
        implements HorarioMedicoService {

    private final HorarioMedicoRepository horarioRepository;
    private final MedicoRepository medicoRepository;
    private final ConsultorioRepository consultorioRepository;

    public HorarioMedicoServiceImpl(
            HorarioMedicoRepository horarioRepository,
            MedicoRepository medicoRepository,
            ConsultorioRepository consultorioRepository
    ) {
        this.horarioRepository = horarioRepository;
        this.medicoRepository = medicoRepository;
        this.consultorioRepository = consultorioRepository;
    }

    @Override
    @Transactional
    public HorarioMedicoResponse registrar(
            HorarioMedicoRequest request
    ) {
        validarCruces(request, null);

        Medico medico = obtenerMedico(request.medicoId());
        Consultorio consultorio = obtenerConsultorio(
                request.consultorioId()
        );

        HorarioMedico horario = new HorarioMedico();
        horario.setMedico(medico);
        horario.setConsultorio(consultorio);
        horario.setDiaSemana(request.diaSemana());
        horario.setHoraInicio(request.horaInicio());
        horario.setHoraFin(request.horaFin());
        horario.setActivo(true);

        return convertirRespuesta(
                horarioRepository.save(horario)
        );
    }

    @Override
    @Transactional
    public HorarioMedicoResponse registrarPropio(Long usuarioId, HorarioMedicoRequest request) {
        Medico propio = medicoRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el perfil del médico"));
        if (!propio.getId().equals(request.medicoId())) {
            throw new RecursoNoEncontradoException("El horario no pertenece al médico autenticado");
        }
        return registrar(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HorarioMedicoResponse> listarPorMedico(
            Long medicoId
    ) {
        obtenerMedico(medicoId);

        return horarioRepository
                .findByMedicoIdAndActivoTrueOrderByDiaSemanaAscHoraInicioAsc(
                        medicoId
                )
                .stream()
                .map(this::convertirRespuesta)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HorarioMedicoResponse> listarTodos() {
        return horarioRepository
                .findAllByOrderByMedicoUsuarioApellidosAscDiaSemanaAscHoraInicioAsc()
                .stream()
                .map(this::convertirRespuesta)
                .toList();
    }

    @Override
    @Transactional
    public HorarioMedicoResponse actualizar(
            Long id,
            HorarioMedicoRequest request
    ) {
        HorarioMedico horario = obtenerHorario(id);

        validarCruces(request, id);

        horario.setMedico(obtenerMedico(request.medicoId()));
        horario.setConsultorio(
                obtenerConsultorio(request.consultorioId())
        );
        horario.setDiaSemana(request.diaSemana());
        horario.setHoraInicio(request.horaInicio());
        horario.setHoraFin(request.horaFin());

        return convertirRespuesta(
                horarioRepository.save(horario)
        );
    }

    @Override
    @Transactional
    public HorarioMedicoResponse cambiarEstado(
            Long id,
            EstadoRequest request
    ) {
        HorarioMedico horario = obtenerHorario(id);
        horario.setActivo(request.activo());

        return convertirRespuesta(
                horarioRepository.save(horario)
        );
    }

    private void validarCruces(
            HorarioMedicoRequest request,
            Long excluirId
    ) {
        long crucesMedico = horarioRepository
                .contarCrucesMedico(
                        request.medicoId(),
                        request.diaSemana(),
                        request.horaInicio(),
                        request.horaFin(),
                        excluirId
                );

        if (crucesMedico > 0) {
            throw new DatoDuplicadoException(
                    "El médico ya tiene un horario en ese intervalo"
            );
        }

        long crucesConsultorio = horarioRepository
                .contarCrucesConsultorio(
                        request.consultorioId(),
                        request.diaSemana(),
                        request.horaInicio(),
                        request.horaFin(),
                        excluirId
                );

        if (crucesConsultorio > 0) {
            throw new DatoDuplicadoException(
                    "El consultorio ya está ocupado en ese intervalo"
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

    private Consultorio obtenerConsultorio(Long id) {
        Consultorio consultorio = consultorioRepository
                .findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Consultorio no encontrado"
                        )
                );

        if (Boolean.FALSE.equals(consultorio.getActivo())) {
            throw new RecursoNoEncontradoException(
                    "El consultorio se encuentra desactivado"
            );
        }

        return consultorio;
    }

    private HorarioMedico obtenerHorario(Long id) {
        return horarioRepository
                .findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Horario médico no encontrado"
                        )
                );
    }

    private HorarioMedicoResponse convertirRespuesta(
            HorarioMedico horario
    ) {
        return new HorarioMedicoResponse(
                horario.getId(),
                horario.getMedico().getId(),
                horario.getMedico().getUsuario().getNombres()
                        + " "
                        + horario.getMedico().getUsuario().getApellidos(),
                horario.getConsultorio().getId(),
                horario.getConsultorio().getCodigo()
                        + " - "
                        + horario.getConsultorio().getNombre(),
                horario.getDiaSemana(),
                horario.getHoraInicio(),
                horario.getHoraFin(),
                horario.getActivo(),
                horario.getFechaCreacion(),
                horario.getFechaActualizacion()
        );
    }
}
