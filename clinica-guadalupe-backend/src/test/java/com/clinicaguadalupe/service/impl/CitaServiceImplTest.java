package com.clinicaguadalupe.service.impl;

import com.clinicaguadalupe.dto.CitaRequest;
import com.clinicaguadalupe.dto.CitaResponse;
import com.clinicaguadalupe.entity.*;
import com.clinicaguadalupe.exception.DatoDuplicadoException;
import com.clinicaguadalupe.repository.*;
import com.clinicaguadalupe.service.PacienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

import static java.time.DayOfWeek.MONDAY;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CitaServiceImplTest {

    private static final String EMAIL = "paciente@ejemplo.com";
    private static final Long MEDICO_ID = 10L;

    private final CitaRepository citas = mock(CitaRepository.class);
    private final PacienteService pacientes = mock(PacienteService.class);
    private final MedicoRepository medicos = mock(MedicoRepository.class);
    private final HorarioMedicoRepository horarios = mock(HorarioMedicoRepository.class);

    private final CitaServiceImpl servicio =
            new CitaServiceImpl(citas, pacientes, medicos, horarios);

    private CitaRequest solicitud;
    private LocalDateTime inicio;
    private LocalDateTime fin;

    @BeforeEach
    void prepararHorario() {
        LocalDate fecha = LocalDate.now().with(TemporalAdjusters.next(MONDAY));
        LocalTime hora = LocalTime.of(9, 0);
        inicio = fecha.atTime(hora);
        fin = inicio.plusMinutes(30);
        solicitud = new CitaRequest(MEDICO_ID, fecha, hora, "  Consulta de control  ");

        Paciente paciente = mock(Paciente.class);
        when(paciente.getId()).thenReturn(20L);
        when(paciente.getUsuario()).thenReturn(new Usuario());

        Especialidad especialidad = mock(Especialidad.class);
        when(especialidad.getNombre()).thenReturn("Cardiología");

        Medico medico = mock(Medico.class);
        when(medico.getId()).thenReturn(MEDICO_ID);
        when(medico.getActivo()).thenReturn(true);
        when(medico.getDuracionConsultaMinutos()).thenReturn(30);
        when(medico.getUsuario()).thenReturn(new Usuario());
        when(medico.getEspecialidad()).thenReturn(especialidad);

        Consultorio consultorio = mock(Consultorio.class);
        when(consultorio.getId()).thenReturn(30L);
        when(consultorio.getCodigo()).thenReturn("CAR-101");

        HorarioMedico horario = mock(HorarioMedico.class);
        when(horario.getHoraInicio()).thenReturn(LocalTime.of(8, 0));
        when(horario.getHoraFin()).thenReturn(LocalTime.of(12, 0));
        when(horario.getConsultorio()).thenReturn(consultorio);

        when(pacientes.obtenerOCrearPorEmail(EMAIL)).thenReturn(paciente);
        when(medicos.findById(MEDICO_ID)).thenReturn(Optional.of(medico));
        when(horarios.findByMedicoIdAndDiaSemanaAndActivoTrueOrderByHoraInicioAsc(
                MEDICO_ID, DiaSemana.LUNES)).thenReturn(List.of(horario));
    }

    @Test
    void debeGuardarReservaDisponibleComoPendiente() {
        when(citas.save(any(Cita.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        CitaResponse respuesta = servicio.reservar(EMAIL, solicitud);

        assertEquals(EstadoCita.PENDIENTE, respuesta.estado());
        assertEquals(inicio, respuesta.fechaHoraInicio());
        assertEquals(fin, respuesta.fechaHoraFin());
        assertEquals("Consulta de control", respuesta.motivo());
        assertEquals(30L, respuesta.consultorioId());
        verify(citas).save(any(Cita.class));
    }

    @Test
    void debeRechazarReservaSiElMedicoYaEstaOcupado() {
        when(citas.contarCrucesMedico(MEDICO_ID, inicio, fin, EstadoCita.CANCELADA))
                .thenReturn(1L);

        DatoDuplicadoException error = assertThrows(
                DatoDuplicadoException.class,
                () -> servicio.reservar(EMAIL, solicitud)
        );

        assertEquals("El médico ya tiene una cita en ese horario", error.getMessage());
        verify(citas, never()).save(any(Cita.class));
    }
    @Test
    void debeRechazarReservaEnFechaPasada() {
        CitaRequest solicitudPasada = new CitaRequest(
                MEDICO_ID, LocalDate.now().minusDays(1),
                LocalTime.of(9, 0), "Consulta de control"
        );

        DatoDuplicadoException error = assertThrows(
                DatoDuplicadoException.class,
                () -> servicio.reservar(EMAIL, solicitudPasada)
        );

        assertEquals("La cita debe reservarse para una fecha y hora futura",
                error.getMessage());
        verify(citas, never()).save(any(Cita.class));
    }

    @Test
    void debeRechazarReservaSiElConsultorioEstaOcupado() {
        when(citas.contarCrucesConsultorio(30L, inicio, fin, EstadoCita.CANCELADA))
                .thenReturn(1L);

        DatoDuplicadoException error = assertThrows(
                DatoDuplicadoException.class,
                () -> servicio.reservar(EMAIL, solicitud)
        );

        assertEquals("El consultorio está ocupado en ese horario", error.getMessage());
        verify(citas, never()).save(any(Cita.class));
    }

    @Test
    void debeCancelarUnaCitaFuturaDelPaciente() {
        var captor = org.mockito.ArgumentCaptor.forClass(Cita.class);
        when(citas.save(any(Cita.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        servicio.reservar(EMAIL, solicitud);
        verify(citas).save(captor.capture());
        Cita cita = captor.getValue();

        when(citas.findByIdAndPacienteUsuarioEmail(99L, EMAIL))
                .thenReturn(Optional.of(cita));

        CitaResponse respuesta = servicio.cancelar(99L, EMAIL);

        assertEquals(EstadoCita.CANCELADA, respuesta.estado());
        assertEquals(EstadoCita.CANCELADA, cita.getEstado());
        verify(citas, times(2)).save(cita);
    }
}