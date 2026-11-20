package com.clinicaguadalupe.service.impl;

import com.clinicaguadalupe.dto.HorarioMedicoRequest;
import com.clinicaguadalupe.entity.*;
import com.clinicaguadalupe.exception.DatoDuplicadoException;
import com.clinicaguadalupe.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HorarioMedicoServiceImplTest {

    private static final Long MEDICO_ID = 10L;
    private static final Long CONSULTORIO_ID = 30L;
    private static final DiaSemana DIA = DiaSemana.LUNES;
    private static final LocalTime INICIO = LocalTime.of(8, 0);
    private static final LocalTime FIN = LocalTime.of(12, 0);

    private final HorarioMedicoRepository horarios = mock(HorarioMedicoRepository.class);
    private final MedicoRepository medicos = mock(MedicoRepository.class);
    private final ConsultorioRepository consultorios = mock(ConsultorioRepository.class);

    private final HorarioMedicoServiceImpl servicio =
            new HorarioMedicoServiceImpl(horarios, medicos, consultorios);

    private final HorarioMedicoRequest solicitud = mock(HorarioMedicoRequest.class);

    @BeforeEach
    void prepararSolicitud() {
        when(solicitud.medicoId()).thenReturn(MEDICO_ID);
        when(solicitud.consultorioId()).thenReturn(CONSULTORIO_ID);
        when(solicitud.diaSemana()).thenReturn(DIA);
        when(solicitud.horaInicio()).thenReturn(INICIO);
        when(solicitud.horaFin()).thenReturn(FIN);
    }

    @Test
    void registraHorarioValidoComoActivo() {
        Medico medico = mock(Medico.class);
        when(medico.getId()).thenReturn(MEDICO_ID);
        when(medico.getActivo()).thenReturn(true);
        when(medico.getUsuario()).thenReturn(new Usuario());

        Consultorio consultorio = mock(Consultorio.class);
        when(consultorio.getId()).thenReturn(CONSULTORIO_ID);
        when(consultorio.getActivo()).thenReturn(true);

        when(medicos.findById(MEDICO_ID)).thenReturn(Optional.of(medico));
        when(consultorios.findById(CONSULTORIO_ID)).thenReturn(Optional.of(consultorio));
        when(horarios.save(any(HorarioMedico.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        servicio.registrar(solicitud);

        var captor = ArgumentCaptor.forClass(HorarioMedico.class);
        verify(horarios).save(captor.capture());
        var guardado = captor.getValue();

        assertSame(medico, guardado.getMedico());
        assertSame(consultorio, guardado.getConsultorio());
        assertEquals(DIA, guardado.getDiaSemana());
        assertEquals(INICIO, guardado.getHoraInicio());
        assertEquals(FIN, guardado.getHoraFin());
        assertEquals(Boolean.TRUE, guardado.getActivo());
    }

    @Test
    void rechazaCruceDelMedicoSinGuardar() {
        when(horarios.contarCrucesMedico(MEDICO_ID, DIA, INICIO, FIN, null))
                .thenReturn(1L);

        var error = assertThrows(DatoDuplicadoException.class,
                () -> servicio.registrar(solicitud));

        assertEquals("El médico ya tiene un horario en ese intervalo", error.getMessage());
        verify(horarios, never()).save(any(HorarioMedico.class));
    }

    @Test
    void rechazaCruceDelConsultorioSinGuardar() {
        when(horarios.contarCrucesConsultorio(CONSULTORIO_ID, DIA, INICIO, FIN, null))
                .thenReturn(1L);

        var error = assertThrows(DatoDuplicadoException.class,
                () -> servicio.registrar(solicitud));

        assertEquals("El consultorio ya está ocupado en ese intervalo", error.getMessage());
        verify(horarios, never()).save(any(HorarioMedico.class));
    }
}