package com.clinicaguadalupe.service.impl;

import com.clinicaguadalupe.dto.*;
import com.clinicaguadalupe.entity.Paciente;
import com.clinicaguadalupe.entity.Rol;
import com.clinicaguadalupe.entity.Usuario;
import com.clinicaguadalupe.exception.CredencialesInvalidasException;
import com.clinicaguadalupe.repository.PacienteRepository;
import com.clinicaguadalupe.repository.UsuarioRepository;
import com.clinicaguadalupe.security.JwtService;
import com.clinicaguadalupe.service.AuthService;
import com.clinicaguadalupe.service.UsuarioService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PacienteRepository pacienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UsuarioService usuarioService;

    public AuthServiceImpl(
            UsuarioRepository usuarioRepository,
            PacienteRepository pacienteRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            UsuarioService usuarioService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.pacienteRepository = pacienteRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.usuarioService = usuarioService;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository
                .findByEmail(request.email().toLowerCase())
                .orElseThrow(() ->
                        new CredencialesInvalidasException(
                                "Correo o contraseña incorrectos"
                        )
                );

        if (!passwordEncoder.matches(
                request.password(),
                usuario.getPassword()
        )) {
            throw new CredencialesInvalidasException(
                    "Correo o contraseña incorrectos"
            );
        }

        if (!usuario.getActivo()) {
            throw new CredencialesInvalidasException(
                    "La cuenta se encuentra desactivada"
            );
        }

        String token = jwtService.generarToken(usuario);

        return new LoginResponse(
                token,
                "Bearer",
                usuario.getId(),
                usuario.getNombres() + " " + usuario.getApellidos(),
                usuario.getEmail(),
                usuario.getRol()
        );
    }

    @Transactional
    @Override
    public UsuarioResponse registrarPaciente(
            RegistroPacienteRequest request
    ) {
        UsuarioRequest usuarioRequest = new UsuarioRequest(
                request.nombres(),
                request.apellidos(),
                request.dni(),
                request.email(),
                request.telefono(),
                request.password(),
                Rol.PACIENTE
        );

        UsuarioResponse respuesta =
                usuarioService.registrar(usuarioRequest);

        Usuario usuario = usuarioRepository
                .findById(respuesta.id())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "No se pudo crear el perfil del paciente"
                        )
                );

        Paciente paciente = new Paciente();
        paciente.setUsuario(usuario);

        pacienteRepository.save(paciente);

        return respuesta;
    }
}