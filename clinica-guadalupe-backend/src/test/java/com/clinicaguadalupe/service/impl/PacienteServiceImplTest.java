package com.clinicaguadalupe.service.impl;

import com.clinicaguadalupe.entity.Paciente;
import com.clinicaguadalupe.entity.Rol;
import com.clinicaguadalupe.entity.Usuario;
import com.clinicaguadalupe.repository.PacienteRepository;
import com.clinicaguadalupe.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PacienteServiceImplTest {
    private static final String EMAIL = "paciente@ejemplo.com";
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final PacienteRepository pacientes = mock(PacienteRepository.class);
    private final PacienteServiceImpl servicio = new PacienteServiceImpl(pacientes, usuarios);

    @Test
    void creaElPerfilFaltanteParaLaCuentaActiva() {
        Usuario usuario = cuenta(Rol.PACIENTE);
        when(pacientes.save(any(Paciente.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        assertSame(usuario, servicio.obtenerOCrearPorEmail(EMAIL).getUsuario());
        verify(pacientes).save(any(Paciente.class));
    }

    @Test
    void reutilizaElPerfilExistente() {
        cuenta(Rol.PACIENTE);
        Paciente perfil = new Paciente();
        when(pacientes.bloquearPorUsuarioId(25L)).thenReturn(Optional.of(perfil));

        assertSame(perfil, servicio.obtenerOCrearPorEmail(EMAIL));
        verify(pacientes, never()).save(any(Paciente.class));
    }

    @Test
    void rechazaUnaCuentaDeOtroRol() {
        cuenta(Rol.MEDICO);

        assertEquals(403, assertThrows(ResponseStatusException.class,
                () -> servicio.obtenerOCrearPorEmail(EMAIL)).getStatusCode().value());
        verifyNoInteractions(pacientes);
    }

    private Usuario cuenta(Rol rol) {
        Usuario usuario = mock(Usuario.class);
        when(usuario.getId()).thenReturn(25L);
        when(usuario.getActivo()).thenReturn(true);
        when(usuario.getRol()).thenReturn(rol);
        when(usuarios.bloquearPorEmail(EMAIL)).thenReturn(Optional.of(usuario));
        return usuario;
    }
}
