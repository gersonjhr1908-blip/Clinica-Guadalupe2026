package com.clinicaguadalupe.service.impl;

import com.clinicaguadalupe.dto.DisponibilidadResponse;
import com.clinicaguadalupe.entity.Cita;
import com.clinicaguadalupe.entity.Consultorio;
import com.clinicaguadalupe.entity.DiaSemana;
import com.clinicaguadalupe.entity.EstadoCita;
import com.clinicaguadalupe.entity.HorarioMedico;
import com.clinicaguadalupe.entity.Medico;
import com.clinicaguadalupe.repository.CitaRepository;
import com.clinicaguadalupe.repository.HorarioMedicoRepository;
import com.clinicaguadalupe.repository.MedicoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DisponibilidadServiceImplTest {

    @Mock
    private MedicoRepository medicoRepository;

    @Mock
    private HorarioMedicoRepository horarioRepository;

    @Mock
    private CitaRepository citaRepository;

    @InjectMocks
    private DisponibilidadServiceImpl disponibilidadService;

    @Test
    void debeOcultarElHorarioOcupado() {

        // Se obtiene el próximo lunes para que la fecha siempre sea futura.
        LocalDate fecha = LocalDate.now()
                .with(
                        TemporalAdjusters.next(
                                DayOfWeek.MONDAY
                        )
                );

        // Médico con consultas de 30 minutos.
        Medico medico = new Medico();
        medico.setActivo(true);
        medico.setDuracionConsultaMinutos(30);

        // Consultorio donde atenderá el médico.
        Consultorio consultorio = new Consultorio();
        consultorio.setCodigo("CAR-101");
        consultorio.setNombre(
                "Consultorio de Cardiología"
        );

        // Horario del médico: lunes de 08:00 a 12:00.
        HorarioMedico horario = new HorarioMedico();
        horario.setMedico(medico);
        horario.setConsultorio(consultorio);
        horario.setDiaSemana(DiaSemana.LUNES);
        horario.setHoraInicio(LocalTime.of(8, 0));
        horario.setHoraFin(LocalTime.of(12, 0));
        horario.setActivo(true);

        // Cita que ocupa el turno de 09:00 a 09:30.
        Cita citaOcupada = new Cita();
        citaOcupada.setFechaHoraInicio(
                LocalDateTime.of(
                        fecha,
                        LocalTime.of(9, 0)
                )
        );
        citaOcupada.setFechaHoraFin(
                LocalDateTime.of(
                        fecha,
                        LocalTime.of(9, 30)
                )
        );
        citaOcupada.setEstado(EstadoCita.CONFIRMADA);

        // Configuración de los repositorios simulados.
        when(medicoRepository.findById(1L))
                .thenReturn(Optional.of(medico));

        when(
                horarioRepository
                        .findByMedicoIdAndDiaSemanaAndActivoTrueOrderByHoraInicioAsc(
                                1L,
                                DiaSemana.LUNES
                        )
        ).thenReturn(List.of(horario));

        when(
                citaRepository.buscarCitasActivasDelMedico(
                        eq(1L),
                        any(LocalDateTime.class),
                        any(LocalDateTime.class),
                        eq(EstadoCita.CANCELADA)
                )
        ).thenReturn(List.of(citaOcupada));

        // Se ejecuta el método que estamos probando.
        List<DisponibilidadResponse> resultado =
                disponibilidadService.obtenerDisponibilidad(
                        1L,
                        fecha
                );

        // De 8 turnos posibles queda 7 porque uno está ocupado.
        assertEquals(7, resultado.size());

        // El turno de las 08:00 debe estar disponible.
        assertTrue(
                resultado.stream().anyMatch(
                        turno ->
                                turno.horaInicio()
                                        .equals(LocalTime.of(8, 0))
                )
        );

        // El turno ocupado de las 09:00 no debe aparecer.
        assertFalse(
                resultado.stream().anyMatch(
                        turno ->
                                turno.horaInicio()
                                        .equals(LocalTime.of(9, 0))
                )
        );
    }

    @Test
    void debeRechazarMedicoInexistente() {
        when(medicoRepository.findById(1L)).thenReturn(Optional.empty());
        org.junit.jupiter.api.Assertions.assertThrows(
                com.clinicaguadalupe.exception.RecursoNoEncontradoException.class,
                () -> disponibilidadService.obtenerDisponibilidad(1L, fechaFutura()));
        org.mockito.Mockito.verifyNoInteractions(horarioRepository, citaRepository);
    }

    @Test
    void debeRechazarMedicoDesactivado() {
        Medico medico = prepararMedico(30);
        medico.setActivo(false);
        org.junit.jupiter.api.Assertions.assertThrows(
                com.clinicaguadalupe.exception.RecursoNoEncontradoException.class,
                () -> disponibilidadService.obtenerDisponibilidad(1L, fechaFutura()));
        org.mockito.Mockito.verifyNoInteractions(horarioRepository, citaRepository);
    }

    @Test
    void debeDevolverVacioParaFechaPasada() {
        prepararMedico(30);
        assertTrue(disponibilidadService.obtenerDisponibilidad(
                1L, LocalDate.now().minusDays(1)).isEmpty());
        org.mockito.Mockito.verifyNoInteractions(horarioRepository, citaRepository);
    }

    @Test
    void debeDevolverVacioSinHorarios() {
        prepararMedico(30);
        prepararAgenda(List.of(), List.of());
        assertTrue(disponibilidadService.obtenerDisponibilidad(1L, fechaFutura()).isEmpty());
    }

    @Test
    void debeOfrecerTodosLosTurnosLibresConSusDatos() {
        prepararMedico(30);
        prepararAgenda(List.of(horario("08:00", "10:00")), List.of());
        List<DisponibilidadResponse> resultado = disponibilidadService.obtenerDisponibilidad(1L, fechaFutura());
        assertEquals(List.of(LocalTime.of(8, 0), LocalTime.of(8, 30),
                LocalTime.of(9, 0), LocalTime.of(9, 30)),
                resultado.stream().map(DisponibilidadResponse::horaInicio).toList());
        assertEquals(LocalTime.of(10, 0), resultado.getLast().horaFin());
        assertEquals(fechaFutura(), resultado.getFirst().fecha());
        assertEquals("CAR-101 - Cardiologia", resultado.getFirst().consultorio());
        // La consulta excluye canceladas; el filtrado SQL se verifica aparte en integración.
        org.mockito.Mockito.verify(citaRepository).buscarCitasActivasDelMedico(
                1L, fechaFutura().atStartOfDay(), fechaFutura().plusDays(1).atStartOfDay(), EstadoCita.CANCELADA);
    }

    @Test
    void debeOcultarAmbosTurnosConSolapamientoParcial() {
        prepararMedico(30);
        prepararAgenda(List.of(horario("08:00", "10:00")), List.of(cita("08:15", "08:45")));
        assertEquals(List.of(LocalTime.of(9, 0), LocalTime.of(9, 30)),
                disponibilidadService.obtenerDisponibilidad(1L, fechaFutura())
                        .stream().map(DisponibilidadResponse::horaInicio).toList());
    }

    @Test
    void debePermitirTurnosContiguosSinSolapamiento() {
        prepararMedico(30);
        prepararAgenda(List.of(horario("08:00", "09:00")),
                List.of(cita("07:30", "08:00"), cita("09:00", "09:30")));
        assertEquals(2, disponibilidadService.obtenerDisponibilidad(1L, fechaFutura()).size());
    }

    @Test
    void debeDevolverVacioCuandoTodaLaJornadaEstaOcupada() {
        prepararMedico(30);
        prepararAgenda(List.of(horario("08:00", "10:00")), List.of(cita("07:00", "11:00")));
        assertTrue(disponibilidadService.obtenerDisponibilidad(1L, fechaFutura()).isEmpty());
    }

    @Test
    void debeDescartarElSobranteQueNoCompletaUnaConsulta() {
        prepararMedico(45);
        prepararAgenda(List.of(horario("08:00", "10:00")), List.of());
        List<DisponibilidadResponse> resultado = disponibilidadService.obtenerDisponibilidad(1L, fechaFutura());
        assertEquals(2, resultado.size());
        assertEquals(LocalTime.of(9, 30), resultado.getLast().horaFin());
    }

    @Test
    void debeDevolverVacioSiLaConsultaNoCabe() {
        prepararMedico(60);
        prepararAgenda(List.of(horario("08:00", "08:30")), List.of());
        assertTrue(disponibilidadService.obtenerDisponibilidad(1L, fechaFutura()).isEmpty());
    }

    @Test
    void debeRespetarLaPausaEntreDosHorarios() {
        prepararMedico(30);
        prepararAgenda(List.of(horario("08:00", "09:00"), horario("11:00", "12:00")), List.of());
        assertEquals(List.of(LocalTime.of(8, 0), LocalTime.of(8, 30),
                LocalTime.of(11, 0), LocalTime.of(11, 30)),
                disponibilidadService.obtenerDisponibilidad(1L, fechaFutura())
                        .stream().map(DisponibilidadResponse::horaInicio).toList());
    }

    @Test
    void noDebeVolverAlInicioDelDiaAlLlegarAMedianoche() {
        prepararMedico(30);
        HorarioMedico horario = org.mockito.Mockito.spy(horario("23:00", "23:45"));
        java.util.concurrent.atomic.AtomicInteger consultas = new java.util.concurrent.atomic.AtomicInteger();
        // Detiene de forma segura el algoritmo anterior si entra en un bucle sin fin.
        org.mockito.Mockito.doAnswer(invocacion -> {
            if (consultas.incrementAndGet() > 100) {
                throw new AssertionError("El cálculo cruzó medianoche y no termina");
            }
            return LocalTime.of(23, 45);
        }).when(horario).getHoraFin();
        prepararAgenda(List.of(horario), List.of());
        List<DisponibilidadResponse> resultado = disponibilidadService.obtenerDisponibilidad(1L, fechaFutura());
        assertEquals(1, resultado.size());
        assertEquals(LocalTime.of(23, 0), resultado.getFirst().horaInicio());
        assertEquals(LocalTime.of(23, 30), resultado.getFirst().horaFin());
    }

    private LocalDate fechaFutura() {
        return LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
    }

    private Medico prepararMedico(int duracion) {
        Medico medico = new Medico();
        medico.setActivo(true);
        medico.setDuracionConsultaMinutos(duracion);
        when(medicoRepository.findById(1L)).thenReturn(Optional.of(medico));
        return medico;
    }

    private HorarioMedico horario(String inicio, String fin) {
        Consultorio consultorio = new Consultorio();
        consultorio.setCodigo("CAR-101");
        consultorio.setNombre("Cardiologia");
        consultorio.setActivo(true);
        HorarioMedico horario = new HorarioMedico();
        horario.setConsultorio(consultorio);
        horario.setHoraInicio(LocalTime.parse(inicio));
        horario.setHoraFin(LocalTime.parse(fin));
        horario.setDiaSemana(DiaSemana.LUNES);
        horario.setActivo(true);
        return horario;
    }

    private Cita cita(String inicio, String fin) {
        Cita cita = new Cita();
        cita.setFechaHoraInicio(fechaFutura().atTime(LocalTime.parse(inicio)));
        cita.setFechaHoraFin(fechaFutura().atTime(LocalTime.parse(fin)));
        cita.setEstado(EstadoCita.CONFIRMADA);
        return cita;
    }

    private void prepararAgenda(List<HorarioMedico> horarios, List<Cita> citas) {
        when(horarioRepository.findByMedicoIdAndDiaSemanaAndActivoTrueOrderByHoraInicioAsc(
                1L, DiaSemana.LUNES)).thenReturn(horarios);
        when(citaRepository.buscarCitasActivasDelMedico(1L, fechaFutura().atStartOfDay(),
                fechaFutura().plusDays(1).atStartOfDay(), EstadoCita.CANCELADA)).thenReturn(citas);
    }
}
