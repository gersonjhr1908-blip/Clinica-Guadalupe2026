package com.clinicaguadalupe.service.impl;

import com.clinicaguadalupe.dto.*;
import com.clinicaguadalupe.entity.Especialidad;
import com.clinicaguadalupe.entity.Medico;
import com.clinicaguadalupe.entity.Rol;
import com.clinicaguadalupe.entity.Usuario;
import com.clinicaguadalupe.exception.DatoDuplicadoException;
import com.clinicaguadalupe.exception.RecursoNoEncontradoException;
import com.clinicaguadalupe.repository.EspecialidadRepository;
import com.clinicaguadalupe.repository.MedicoRepository;
import com.clinicaguadalupe.repository.UsuarioRepository;
import com.clinicaguadalupe.service.MedicoService;
import com.clinicaguadalupe.service.UsuarioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MedicoServiceImpl implements MedicoService {

    private final MedicoRepository medicoRepository;
    private final EspecialidadRepository especialidadRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    public MedicoServiceImpl(
            MedicoRepository medicoRepository,
            EspecialidadRepository especialidadRepository,
            UsuarioRepository usuarioRepository,
            UsuarioService usuarioService
    ) {
        this.medicoRepository = medicoRepository;
        this.especialidadRepository = especialidadRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
    }

    @Override
    @Transactional
    public MedicoResponse registrar(MedicoRequest request) {

        if (medicoRepository.existsByCmp(request.cmp())) {
            throw new DatoDuplicadoException(
                    "Ya existe un médico registrado con ese CMP"
            );
        }

        Especialidad especialidad = obtenerEspecialidad(
                request.especialidadId()
        );

        UsuarioRequest usuarioRequest = new UsuarioRequest(
                request.nombres(),
                request.apellidos(),
                request.dni(),
                request.email(),
                request.telefono(),
                request.password(),
                Rol.MEDICO
        );

        UsuarioResponse usuarioRegistrado =
                usuarioService.registrar(usuarioRequest);

        Usuario usuario = usuarioRepository
                .findById(usuarioRegistrado.id())
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "No se encontró el usuario del médico"
                        )
                );

        Medico medico = new Medico();
        medico.setUsuario(usuario);
        medico.setEspecialidad(especialidad);
        medico.setCmp(request.cmp());
        medico.setPrecioConsulta(request.precioConsulta());
        medico.setDuracionConsultaMinutos(
                request.duracionConsultaMinutos()
        );
        medico.setBiografia(request.biografia());
        medico.setActivo(true);

        return convertirRespuesta(
                medicoRepository.save(medico)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicoResponse> listarActivos() {
        return medicoRepository
                .findByActivoTrueOrderByUsuarioApellidosAsc()
                .stream()
                .map(this::convertirRespuesta)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicoResponse> listarTodos() {
        return medicoRepository
                .findAllByOrderByUsuarioApellidosAsc()
                .stream()
                .map(this::convertirRespuesta)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MedicoResponse buscarPorId(Long id) {
        return convertirRespuesta(obtenerMedico(id));
    }

    @Override
    @Transactional(readOnly = true)
    public MedicoResponse buscarPorUsuarioId(Long usuarioId) {
        return medicoRepository.findByUsuarioId(usuarioId)
                .map(this::convertirRespuesta)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el perfil del médico"));
    }

    @Override
    @Transactional
    public MedicoResponse actualizarMiPerfil(Long usuarioId, MedicoPerfilActualizarRequest request) {
        Medico medico = medicoRepository.findByUsuarioId(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el perfil del médico"));
        Usuario usuario = medico.getUsuario();
        String email = request.email().trim().toLowerCase();
        if (usuarioRepository.existsByEmailAndIdNot(email, usuario.getId())) {
            throw new DatoDuplicadoException("Ya existe un usuario con ese correo");
        }
        if (usuarioRepository.existsByDniAndIdNot(request.dni(), usuario.getId())) {
            throw new DatoDuplicadoException("Ya existe un usuario con ese DNI");
        }
        usuario.setNombres(request.nombres().trim());
        usuario.setApellidos(request.apellidos().trim());
        usuario.setDni(request.dni().trim());
        usuario.setEmail(email);
        usuario.setTelefono(request.telefono().trim());
        medico.setEspecialidad(obtenerEspecialidad(request.especialidadId()));
        if (medicoRepository.existsByCmpAndIdNot(request.cmp(), medico.getId())) {
            throw new DatoDuplicadoException("Ya existe otro médico con ese CMP");
        }
        medico.setCmp(request.cmp().trim());
        medico.setPrecioConsulta(request.precioConsulta());
        medico.setDuracionConsultaMinutos(request.duracionConsultaMinutos());
        medico.setBiografia(request.biografia());
        usuarioRepository.save(usuario);
        return convertirRespuesta(medicoRepository.save(medico));
    }

    @Override
    @Transactional
    public MedicoResponse actualizar(
            Long id,
            MedicoActualizarRequest request
    ) {
        Medico medico = obtenerMedico(id);

        if (medicoRepository.existsByCmpAndIdNot(
                request.cmp(),
                id
        )) {
            throw new DatoDuplicadoException(
                    "Ya existe otro médico con ese CMP"
            );
        }

        Especialidad especialidad = obtenerEspecialidad(
                request.especialidadId()
        );

        medico.setEspecialidad(especialidad);
        medico.setCmp(request.cmp());
        medico.setPrecioConsulta(request.precioConsulta());
        medico.setDuracionConsultaMinutos(
                request.duracionConsultaMinutos()
        );
        medico.setBiografia(request.biografia());

        return convertirRespuesta(
                medicoRepository.save(medico)
        );
    }

    @Override
    @Transactional
    public MedicoResponse cambiarEstado(
            Long id,
            EstadoRequest request
    ) {
        Medico medico = obtenerMedico(id);

        medico.setActivo(request.activo());
        medico.getUsuario().setActivo(request.activo());

        usuarioRepository.save(medico.getUsuario());

        return convertirRespuesta(
                medicoRepository.save(medico)
        );
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Medico medico = obtenerMedico(id);
        usuarioService.eliminar(medico.getUsuario().getId());
    }

    private Medico obtenerMedico(Long id) {
        return medicoRepository
                .findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Médico no encontrado"
                        )
                );
    }

    private Especialidad obtenerEspecialidad(Long id) {
        Especialidad especialidad = especialidadRepository
                .findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Especialidad no encontrada"
                        )
                );

        if (Boolean.FALSE.equals(especialidad.getActivo())) {
            throw new RecursoNoEncontradoException(
                    "La especialidad se encuentra desactivada"
            );
        }

        return especialidad;
    }

    private MedicoResponse convertirRespuesta(Medico medico) {
        Usuario usuario = medico.getUsuario();
        Especialidad especialidad = medico.getEspecialidad();

        return new MedicoResponse(
                medico.getId(),
                usuario.getId(),
                usuario.getNombres(),
                usuario.getApellidos(),
                usuario.getDni(),
                usuario.getEmail(),
                usuario.getTelefono(),
                especialidad.getId(),
                especialidad.getNombre(),
                medico.getCmp(),
                medico.getPrecioConsulta(),
                medico.getDuracionConsultaMinutos(),
                medico.getBiografia(),
                medico.getActivo(),
                medico.getFechaCreacion(),
                medico.getFechaActualizacion()
        );
    }
}
