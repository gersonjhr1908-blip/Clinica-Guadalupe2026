package com.clinicaguadalupe.service.impl;

import com.clinicaguadalupe.dto.*;
import com.clinicaguadalupe.entity.*;
import com.clinicaguadalupe.exception.CredencialesInvalidasException;
import com.clinicaguadalupe.repository.PacienteRepository;
import com.clinicaguadalupe.repository.UsuarioRepository;
import com.clinicaguadalupe.security.JwtService;
import com.clinicaguadalupe.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PacienteRepository pacienteRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private UsuarioService usuarioService;
    @InjectMocks private AuthServiceImpl servicio;

    @Test
    void loginDebeNormalizarCorreoYDevolverToken() {
        Usuario usuario = usuario();
        when(usuarioRepository.findByEmail("paciente@example.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("clave-prueba", "hash-prueba")).thenReturn(true);
        when(jwtService.generarToken(usuario)).thenReturn("token-prueba");

        LoginResponse respuesta = servicio.login(new LoginRequest("PACIENTE@EXAMPLE.COM", "clave-prueba"));

        assertEquals("token-prueba", respuesta.token());
        assertEquals("Bearer", respuesta.tipo());
        assertEquals(4L, respuesta.usuarioId());
        assertEquals(Rol.PACIENTE, respuesta.rol());
        assertEquals("Paciente Prueba", respuesta.nombreCompleto());
    }

    @Test
    void loginDebeRechazarCorreoInexistenteSinGenerarToken() {
        when(usuarioRepository.findByEmail("nadie@example.com")).thenReturn(Optional.empty());
        assertThrows(CredencialesInvalidasException.class,
                () -> servicio.login(new LoginRequest("nadie@example.com", "clave-prueba")));
        verifyNoInteractions(passwordEncoder, jwtService);
    }

    @Test
    void loginDebeRechazarPasswordIncorrectaSinGenerarToken() {
        when(usuarioRepository.findByEmail("paciente@example.com")).thenReturn(Optional.of(usuario()));
        when(passwordEncoder.matches("incorrecta", "hash-prueba")).thenReturn(false);
        assertThrows(CredencialesInvalidasException.class,
                () -> servicio.login(new LoginRequest("paciente@example.com", "incorrecta")));
        verifyNoInteractions(jwtService);
    }

    @Test
    void loginDebeRechazarCuentaDesactivadaAunqueLaPasswordSeaCorrecta() {
        Usuario usuario = usuario();
        usuario.setActivo(false);
        when(usuarioRepository.findByEmail("paciente@example.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("clave-prueba", "hash-prueba")).thenReturn(true);
        assertThrows(CredencialesInvalidasException.class,
                () -> servicio.login(new LoginRequest("paciente@example.com", "clave-prueba")));
        verifyNoInteractions(jwtService);
    }

    @Test
    void registroPublicoDebeAsignarRolPacienteYVincularSuPerfil() {
        Usuario usuario = usuario();
        UsuarioResponse respuesta = new UsuarioResponse(4L, "Paciente", "Prueba", "70000004",
                "paciente@example.com", "900000004", Rol.PACIENTE, true, null);
        when(usuarioService.registrar(any(UsuarioRequest.class))).thenReturn(respuesta);
        when(usuarioRepository.findById(4L)).thenReturn(Optional.of(usuario));

        assertSame(respuesta, servicio.registrarPaciente(registro()));

        ArgumentCaptor<UsuarioRequest> cuenta = ArgumentCaptor.forClass(UsuarioRequest.class);
        verify(usuarioService).registrar(cuenta.capture());
        assertEquals(Rol.PACIENTE, cuenta.getValue().rol());
        assertEquals("paciente@example.com", cuenta.getValue().email());
        ArgumentCaptor<Paciente> perfil = ArgumentCaptor.forClass(Paciente.class);
        verify(pacienteRepository).save(perfil.capture());
        assertSame(usuario, perfil.getValue().getUsuario());
    }

    @Test
    void registroNoDebeCrearPerfilSiFallaLaCreacionDeCuenta() {
        when(usuarioService.registrar(any(UsuarioRequest.class)))
                .thenThrow(new IllegalStateException("Fallo simulado"));
        assertThrows(IllegalStateException.class, () -> servicio.registrarPaciente(registro()));
        verifyNoInteractions(usuarioRepository, pacienteRepository);
    }

    private RegistroPacienteRequest registro() {
        return new RegistroPacienteRequest("Paciente", "Prueba", "70000004",
                "paciente@example.com", "900000004", "clave-prueba");
    }

    private Usuario usuario() {
        Usuario usuario = new Usuario();
        usuario.setId(4L);
        usuario.setNombres("Paciente");
        usuario.setApellidos("Prueba");
        usuario.setEmail("paciente@example.com");
        usuario.setPassword("hash-prueba");
        usuario.setRol(Rol.PACIENTE);
        usuario.setActivo(true);
        return usuario;
    }
}
