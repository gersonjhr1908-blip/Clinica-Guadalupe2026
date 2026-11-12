package com.clinicaguadalupe.service.impl;

import com.clinicaguadalupe.dto.EspecialidadRequest;
import com.clinicaguadalupe.dto.EspecialidadResponse;
import com.clinicaguadalupe.entity.Especialidad;
import com.clinicaguadalupe.exception.DatoDuplicadoException;
import com.clinicaguadalupe.exception.RecursoNoEncontradoException;
import com.clinicaguadalupe.repository.EspecialidadRepository;
import com.clinicaguadalupe.service.EspecialidadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EspecialidadServiceImpl
        implements EspecialidadService {

    private final EspecialidadRepository especialidadRepository;

    public EspecialidadServiceImpl(
            EspecialidadRepository especialidadRepository
    ) {
        this.especialidadRepository = especialidadRepository;
    }

    @Override
    public EspecialidadResponse registrar(
            EspecialidadRequest request
    ) {
        String nombre = request.nombre().trim();

        if (especialidadRepository
                .existsByNombreIgnoreCase(nombre)) {
            throw new DatoDuplicadoException(
                    "La especialidad ya está registrada"
            );
        }

        Especialidad especialidad = new Especialidad();
        especialidad.setNombre(nombre);
        especialidad.setDescripcion(request.descripcion());
        especialidad.setActivo(true);

        return convertirRespuesta(
                especialidadRepository.save(especialidad)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<EspecialidadResponse> listarActivas() {
        return especialidadRepository
                .findByActivoTrueOrderByNombreAsc()
                .stream()
                .map(this::convertirRespuesta)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EspecialidadResponse> listarTodas() {
        return especialidadRepository
                .findAllByOrderByNombreAsc()
                .stream()
                .map(this::convertirRespuesta)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EspecialidadResponse buscarPorId(Long id) {
        return convertirRespuesta(buscarEntidad(id));
    }

    @Override
    public EspecialidadResponse actualizar(
            Long id,
            EspecialidadRequest request
    ) {
        Especialidad especialidad = buscarEntidad(id);
        String nombre = request.nombre().trim();

        if (especialidadRepository
                .existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new DatoDuplicadoException(
                    "La especialidad ya está registrada"
            );
        }

        especialidad.setNombre(nombre);
        especialidad.setDescripcion(request.descripcion());

        return convertirRespuesta(
                especialidadRepository.save(especialidad)
        );
    }

    @Override
    public EspecialidadResponse cambiarEstado(
            Long id,
            Boolean activo
    ) {
        Especialidad especialidad = buscarEntidad(id);
        especialidad.setActivo(activo);

        return convertirRespuesta(
                especialidadRepository.save(especialidad)
        );
    }

    private Especialidad buscarEntidad(Long id) {
        return especialidadRepository
                .findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "No se encontró la especialidad con ID " + id
                        )
                );
    }

    private EspecialidadResponse convertirRespuesta(
            Especialidad especialidad
    ) {
        return new EspecialidadResponse(
                especialidad.getId(),
                especialidad.getNombre(),
                especialidad.getDescripcion(),
                especialidad.getActivo(),
                especialidad.getFechaCreacion(),
                especialidad.getFechaActualizacion()
        );
    }
}