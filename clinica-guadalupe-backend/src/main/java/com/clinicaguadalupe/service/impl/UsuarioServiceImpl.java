package com.clinicaguadalupe.service.impl;

import com.clinicaguadalupe.dto.UsuarioRequest;
import com.clinicaguadalupe.dto.UsuarioResponse;
import com.clinicaguadalupe.entity.Usuario;
import com.clinicaguadalupe.exception.DatoDuplicadoException;
import com.clinicaguadalupe.repository.UsuarioRepository;
import com.clinicaguadalupe.service.UsuarioService;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.clinicaguadalupe.exception.RecursoNoEncontradoException;
import com.clinicaguadalupe.dto.UsuarioActualizarRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.clinicaguadalupe.dto.CambiarPasswordRequest;

import java.util.List;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
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

        return convertirRespuesta(usuarioRepository.save(usuario));
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
