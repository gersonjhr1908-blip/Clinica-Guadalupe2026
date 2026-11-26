package com.clinicaguadalupe.service.impl;

import com.clinicaguadalupe.dto.ConsultorioRequest;
import com.clinicaguadalupe.dto.ConsultorioResponse;
import com.clinicaguadalupe.dto.EstadoRequest;
import com.clinicaguadalupe.entity.Consultorio;
import com.clinicaguadalupe.exception.DatoDuplicadoException;
import com.clinicaguadalupe.exception.RecursoNoEncontradoException;
import com.clinicaguadalupe.exception.OperacionNoPermitidaException;
import com.clinicaguadalupe.repository.CitaRepository;
import com.clinicaguadalupe.repository.ConsultorioRepository;
import com.clinicaguadalupe.repository.HorarioMedicoRepository;
import com.clinicaguadalupe.service.ConsultorioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConsultorioServiceImpl
        implements ConsultorioService {

    private final ConsultorioRepository consultorioRepository;

    private final CitaRepository citaRepository;
    private final HorarioMedicoRepository horarioRepository;

    public ConsultorioServiceImpl(
            ConsultorioRepository consultorioRepository,
            CitaRepository citaRepository,
            HorarioMedicoRepository horarioRepository
    ) {
        this.consultorioRepository = consultorioRepository;
        this.citaRepository = citaRepository;
        this.horarioRepository = horarioRepository;
    }

    @Override
    @Transactional
    public ConsultorioResponse registrar(
            ConsultorioRequest request
    ) {
        String codigo = request.codigo()
                .trim()
                .toUpperCase();

        if (consultorioRepository
                .existsByCodigoIgnoreCase(codigo)) {
            throw new DatoDuplicadoException(
                    "Ya existe un consultorio con ese código"
            );
        }

        Consultorio consultorio = new Consultorio();
        consultorio.setCodigo(codigo);
        consultorio.setNombre(request.nombre().trim());
        consultorio.setPiso(request.piso());
        consultorio.setDescripcion(request.descripcion());
        consultorio.setActivo(true);

        return convertirRespuesta(
                consultorioRepository.save(consultorio)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsultorioResponse> listarActivos() {
        return consultorioRepository
                .findByActivoTrueOrderByCodigoAsc()
                .stream()
                .map(this::convertirRespuesta)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsultorioResponse> listarTodos() {
        return consultorioRepository
                .findAllByOrderByCodigoAsc()
                .stream()
                .map(this::convertirRespuesta)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ConsultorioResponse buscarPorId(Long id) {
        return convertirRespuesta(obtenerConsultorio(id));
    }

    @Override
    @Transactional
    public ConsultorioResponse actualizar(
            Long id,
            ConsultorioRequest request
    ) {
        Consultorio consultorio = obtenerConsultorio(id);

        String codigo = request.codigo()
                .trim()
                .toUpperCase();

        if (consultorioRepository
                .existsByCodigoIgnoreCaseAndIdNot(
                        codigo,
                        id
                )) {
            throw new DatoDuplicadoException(
                    "Ya existe otro consultorio con ese código"
            );
        }

        consultorio.setCodigo(codigo);
        consultorio.setNombre(request.nombre().trim());
        consultorio.setPiso(request.piso());
        consultorio.setDescripcion(request.descripcion());

        return convertirRespuesta(
                consultorioRepository.save(consultorio)
        );
    }

    @Override
    @Transactional
    public ConsultorioResponse cambiarEstado(
            Long id,
            EstadoRequest request
    ) {
        Consultorio consultorio = obtenerConsultorio(id);
        consultorio.setActivo(request.activo());

        return convertirRespuesta(
                consultorioRepository.save(consultorio)
        );
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Consultorio consultorio = obtenerConsultorio(id);
        if (citaRepository.existsByConsultorioId(id) || horarioRepository.existsByConsultorioId(id)) {
            throw new OperacionNoPermitidaException(
                    "El consultorio tiene citas u horarios asociados. Desactívalo para dejar de ofrecerlo."
            );
        }
        consultorioRepository.delete(consultorio);
        consultorioRepository.flush();
    }

    private Consultorio obtenerConsultorio(Long id) {
        return consultorioRepository
                .findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Consultorio no encontrado"
                        )
                );
    }

    private ConsultorioResponse convertirRespuesta(
            Consultorio consultorio
    ) {
        return new ConsultorioResponse(
                consultorio.getId(),
                consultorio.getCodigo(),
                consultorio.getNombre(),
                consultorio.getPiso(),
                consultorio.getDescripcion(),
                consultorio.getActivo(),
                consultorio.getFechaCreacion(),
                consultorio.getFechaActualizacion()
        );
    }
}