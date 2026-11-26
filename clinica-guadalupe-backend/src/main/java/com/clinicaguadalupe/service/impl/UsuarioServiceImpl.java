package com.clinicaguadalupe.service.impl;

import com.clinicaguadalupe.dto.UsuarioRequest;
import com.clinicaguadalupe.dto.UsuarioResponse;
import com.clinicaguadalupe.entity.Usuario;
import com.clinicaguadalupe.exception.DatoDuplicadoException;
import com.clinicaguadalupe.entity.Paciente;
import com.clinicaguadalupe.repository.UsuarioRepository;
import com.clinicaguadalupe.service.UsuarioService;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.clinicaguadalupe.exception.RecursoNoEncontradoException;
import com.clinicaguadalupe.dto.UsuarioActualizarRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.clinicaguadalupe.dto.CambiarPasswordRequest;
import com.clinicaguadalupe.entity.Rol;
import com.clinicaguadalupe.exception.OperacionNoPermitidaException;
import com.clinicaguadalupe.repository.CitaRepository;
import com.clinicaguadalupe.repository.HorarioMedicoRepository;
import com.clinicaguadalupe.repository.MedicoRepository;
import com.clinicaguadalupe.repository.PacienteRepository;

import java.util.List;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final PacienteRepository pacienteRepository;
    private final MedicoRepository medicoRepository;
    private final HorarioMedicoRepository horarioMedicoRepository;
    private final CitaRepository citaRepository;

    public UsuarioServiceImpl(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            PacienteRepository pacienteRepository,
            MedicoRepository medicoRepository,
            HorarioMedicoRepository horarioMedicoRepository,
            CitaRepository citaRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.pacienteRepository = pacienteRepository;
        this.medicoRepository = medicoRepository;
        this.horarioMedicoRepository = horarioMedicoRepository;
        this.citaRepository = citaRepository;
    }

    @Override
    @Transactional
    public UsuarioResponse registrar(UsuarioRequest request) {

        if (usuarioRepository.existsByEmail(request.email())) {
            throw new DatoDuplicadoException("El correo ya está registrado");
        }

        if (usuarioRepository.existsByDni(request.dni())) {
            throw new DatoDuplicadoException("El DNI ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNombres(request.nombres());
        usuario.setApellidos(request.apellidos());
        usuario.setDni(request.dni());
        usuario.setEmail(request.email().toLowerCase());
        usuario.setTelefono(request.telefono());
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setRol(request.rol());
        usuario.setActivo(true);

        Usuario guardado = usuarioRepository.save(usuario);
        if (guardado.getRol() == Rol.PACIENTE) {
            Paciente paciente = new Paciente();
            paciente.setUsuario(guardado);
            pacienteRepository.save(paciente);
        }
        return convertirRespuesta(guardado);
    }

    @Override
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::convertirRespuesta)
                .toList();
    }

    @Override
    public UsuarioResponse buscarPorId(Long id) {
        return convertirRespuesta(buscarEntidad(id));
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorEmail(String email) {
        return convertirRespuesta(usuarioRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado")));
    }

    @Override
    public UsuarioResponse cambiarEstado(Long id, Boolean activo) {
        Usuario usuario = buscarEntidad(id);
        usuario.setActivo(activo);

        return convertirRespuesta(usuarioRepository.save(usuario));
    }

    @Override
    public UsuarioResponse actualizar(
            Long id,
            UsuarioActualizarRequest request
    ) {
        Usuario usuario = buscarEntidad(id);
        String email = request.email().toLowerCase();

        if (usuarioRepository.existsByEmailAndIdNot(email, id)) {
            throw new DatoDuplicadoException("El correo ya está registrado");
        }

        if (usuarioRepository.existsByDniAndIdNot(request.dni(), id)) {
            throw new DatoDuplicadoException("El DNI ya está registrado");
        }

        usuario.setNombres(request.nombres());
        usuario.setApellidos(request.apellidos());
        usuario.setDni(request.dni());
        usuario.setEmail(email);
        usuario.setTelefono(request.telefono());
        usuario.setRol(request.rol());

        return convertirRespuesta(usuarioRepository.save(usuario));
    }

    @Override
    public void cambiarPassword(
            Long id,
            CambiarPasswordRequest request
    ) {
        Usuario usuario = buscarEntidad(id);

        usuario.setPassword(
                passwordEncoder.encode(request.nuevaPassword())
        );

        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Usuario usuario = buscarEntidad(id);

        if (usuario.getRol() == Rol.ADMINISTRADOR) {
            throw new OperacionNoPermitidaException(
                    "No se puede eliminar una cuenta administradora"
            );
        }

        if (citaRepository.existsByPacienteUsuarioId(id)
                || citaRepository.existsByMedicoUsuarioId(id)) {
            throw new OperacionNoPermitidaException(
                    "No se puede eliminar una cuenta con citas registradas. "
                            + "Desactívala para conservar el historial clínico."
            );
        }

        pacienteRepository.findByUsuarioId(id)
                .ifPresent(pacienteRepository::delete);

        medicoRepository.findByUsuarioId(id).ifPresent(medico -> {
            horarioMedicoRepository.deleteAll(
                    horarioMedicoRepository.findByMedicoId(medico.getId())
            );
            medicoRepository.delete(medico);
        });

        usuarioRepository.delete(usuario);
    }

    private Usuario buscarEntidad(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "No se encontró el usuario con ID " + id
                        )
                );
    }

    private UsuarioResponse convertirRespuesta(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombres(),
                usuario.getApellidos(),
                usuario.getDni(),
                usuario.getEmail(),
                usuario.getTelefono(),
                usuario.getRol(),
                usuario.getActivo(),
                usuario.getFechaCreacion()
        );
    }
}
