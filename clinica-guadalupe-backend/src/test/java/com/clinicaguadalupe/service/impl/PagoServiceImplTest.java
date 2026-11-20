package com.clinicaguadalupe.service.impl;

import com.clinicaguadalupe.dto.PagoRequest;
import com.clinicaguadalupe.entity.*;
import com.clinicaguadalupe.repository.CitaRepository;
import com.clinicaguadalupe.repository.PagoRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.clinicaguadalupe.exception.DatoDuplicadoException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PagoServiceImplTest {

    @Test
    void debeGuardarPagoYapeAprobadoCuandoLaCitaEstaConfirmada() {
        PagoRepository pagoRepository = mock(PagoRepository.class);
        CitaRepository citaRepository = mock(CitaRepository.class);
        PagoServiceImpl servicio = new PagoServiceImpl(pagoRepository, citaRepository);

        Long citaId = 1L;
        String email = "paciente@ejemplo.com";
        BigDecimal precio = new BigDecimal("80.00");

        Usuario usuarioPaciente = new Usuario();
        usuarioPaciente.setNombres("Ana");
        usuarioPaciente.setApellidos("Pérez");

        Paciente paciente = new Paciente();
        paciente.setUsuario(usuarioPaciente);

        Usuario usuarioMedico = new Usuario();
        usuarioMedico.setNombres("Luis");
        usuarioMedico.setApellidos("Rojas");

        Medico medico = new Medico();
        medico.setUsuario(usuarioMedico);
        medico.setPrecioConsulta(precio);

        Cita cita = mock(Cita.class);
        when(cita.getId()).thenReturn(citaId);
        when(cita.getEstado()).thenReturn(EstadoCita.CONFIRMADA);
        when(cita.getPaciente()).thenReturn(paciente);
        when(cita.getMedico()).thenReturn(medico);

        when(citaRepository.findByIdAndPacienteUsuarioEmail(citaId, email))
                .thenReturn(Optional.of(cita));
        when(pagoRepository.existsByCitaId(citaId)).thenReturn(false);
        when(pagoRepository.save(any(Pago.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        servicio.procesarPago(email, new PagoRequest(citaId, MetodoPago.YAPE));

        ArgumentCaptor<Pago> captor = ArgumentCaptor.forClass(Pago.class);
        verify(pagoRepository).save(captor.capture());

        Pago guardado = captor.getValue();
        assertSame(cita, guardado.getCita());
        assertEquals(EstadoPago.APROBADO, guardado.getEstado());
        assertEquals(MetodoPago.YAPE, guardado.getMetodoPago());
        assertEquals(precio, guardado.getMonto());
        assertNotNull(guardado.getFechaPago());
        assertNotNull(guardado.getCodigoOperacion());
        assertTrue(guardado.getCodigoOperacion().startsWith("CG-"));
    }
    @Test
    void debeGuardarPagoEnEfectivoComoPendiente() {
        PagoRepository pagoRepository = mock(PagoRepository.class);
        CitaRepository citaRepository = mock(CitaRepository.class);
        PagoServiceImpl servicio = new PagoServiceImpl(pagoRepository, citaRepository);

        Long citaId = 2L;
        String email = "paciente@ejemplo.com";
        BigDecimal precio = new BigDecimal("80.00");

        Paciente paciente = new Paciente();
        paciente.setUsuario(new Usuario());

        Medico medico = new Medico();
        medico.setUsuario(new Usuario());
        medico.setPrecioConsulta(precio);

        Cita cita = mock(Cita.class);
        when(cita.getId()).thenReturn(citaId);
        when(cita.getEstado()).thenReturn(EstadoCita.CONFIRMADA);
        when(cita.getPaciente()).thenReturn(paciente);
        when(cita.getMedico()).thenReturn(medico);

        when(citaRepository.findByIdAndPacienteUsuarioEmail(citaId, email))
                .thenReturn(Optional.of(cita));
        when(pagoRepository.existsByCitaId(citaId)).thenReturn(false);
        when(pagoRepository.save(any(Pago.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        servicio.procesarPago(email, new PagoRequest(citaId, MetodoPago.EFECTIVO));

        ArgumentCaptor<Pago> captor = ArgumentCaptor.forClass(Pago.class);
        verify(pagoRepository).save(captor.capture());

        Pago guardado = captor.getValue();
        assertEquals(MetodoPago.EFECTIVO, guardado.getMetodoPago());
        assertEquals(EstadoPago.PENDIENTE, guardado.getEstado());
        assertEquals(precio, guardado.getMonto());
        assertNull(guardado.getCodigoOperacion());
        assertNull(guardado.getFechaPago());
    }

    @Test
    void debeRechazarUnSegundoPagoParaLaMismaCita() {
        PagoRepository pagoRepository = mock(PagoRepository.class);
        CitaRepository citaRepository = mock(CitaRepository.class);
        PagoServiceImpl servicio = new PagoServiceImpl(pagoRepository, citaRepository);

        Long citaId = 3L;
        String email = "paciente@ejemplo.com";

        Cita cita = mock(Cita.class);
        when(cita.getId()).thenReturn(citaId);
        when(cita.getEstado()).thenReturn(EstadoCita.CONFIRMADA);
        when(citaRepository.findByIdAndPacienteUsuarioEmail(citaId, email))
                .thenReturn(Optional.of(cita));
        when(pagoRepository.existsByCitaId(citaId)).thenReturn(true);

        DatoDuplicadoException error = assertThrows(
                DatoDuplicadoException.class,
                () -> servicio.procesarPago(email, new PagoRequest(citaId, MetodoPago.YAPE))
        );

        assertEquals("La cita ya tiene un pago registrado", error.getMessage());
        verify(pagoRepository, never()).save(any(Pago.class));
    }
    @Test
    void debeRechazarPagoSinMetodoAntesDeConsultarLaCita() {
        var citaRepository = mock(CitaRepository.class);
        var pagoRepository = mock(PagoRepository.class);
        var servicio = new PagoServiceImpl(pagoRepository, citaRepository);
        var solicitud = new PagoRequest(1L, null);

        var error = assertThrows(
                IllegalArgumentException.class,
                () -> servicio.procesarPago("paciente@ejemplo.com", solicitud)
        );

        assertEquals("El método de pago es obligatorio", error.getMessage());
        verifyNoInteractions(citaRepository, pagoRepository);
    }
}