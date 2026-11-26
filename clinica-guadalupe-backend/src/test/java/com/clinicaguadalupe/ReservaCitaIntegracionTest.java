package com.clinicaguadalupe;

import com.clinicaguadalupe.entity.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static java.time.DayOfWeek.MONDAY;
import static java.time.temporal.TemporalAdjusters.next;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request
        .SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integracion")
@Transactional
class ReservaCitaIntegracionTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EntityManager em;

    @Test
    void debeReservarDesdeApiYPersistirCitaPendiente() throws Exception {
        // Datos ficticios creados exclusivamente en H2.
        Usuario usuarioPaciente = crearUsuario(
                "Paciente", "Integracion", "90000001",
                "paciente.integracion@example.com", Rol.PACIENTE
        );

        Paciente paciente = new Paciente();
        paciente.setUsuario(usuarioPaciente);
        em.persist(paciente);

        Usuario usuarioMedico = crearUsuario(
                "Medico", "Integracion", "90000002",
                "medico.integracion@example.com", Rol.MEDICO
        );

        Especialidad especialidad = new Especialidad();
        especialidad.setNombre("Especialidad Integracion");
        em.persist(especialidad);

        Medico medico = new Medico();
        medico.setUsuario(usuarioMedico);
        medico.setEspecialidad(especialidad);
        medico.setCmp("CMP-TEST-01");
        medico.setPrecioConsulta(new BigDecimal("80.00"));
        medico.setDuracionConsultaMinutos(30);
        medico.setActivo(true);
        em.persist(medico);

        Consultorio consultorio = new Consultorio();
        consultorio.setCodigo("TEST-101");
        consultorio.setNombre("Consultorio de integracion");
        consultorio.setPiso(1);
        consultorio.setActivo(true);
        em.persist(consultorio);

        LocalDate fecha = LocalDate.now().with(next(MONDAY));
        LocalTime hora = LocalTime.of(9, 0);

        HorarioMedico horario = new HorarioMedico();
        horario.setMedico(medico);
        horario.setConsultorio(consultorio);
        horario.setDiaSemana(DiaSemana.LUNES);
        horario.setHoraInicio(LocalTime.of(8, 0));
        horario.setHoraFin(LocalTime.of(12, 0));
        horario.setActivo(true);
        em.persist(horario);
        em.flush();

        long cantidadAntes = em.createQuery(
                "select count(c) from Cita c", Long.class
        ).getSingleResult();

        String solicitud = """
                {
                  "medicoId": %d,
                  "fecha": "%s",
                  "hora": "09:00",
                  "motivo": "Prueba de integracion de reserva"
                }
                """.formatted(medico.getId(), fecha);

        // Autenticacion de prueba; controlador, servicio y JPA reales.
        mvc.perform(post("/api/citas")
                        .with(jwt()
                                .jwt(token -> token.subject(
                                        usuarioPaciente.getEmail()
                                ))
                                .authorities(new SimpleGrantedAuthority(
                                        "ROLE_PACIENTE"
                                )))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(solicitud))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.codigo").isNotEmpty());

        // Fuerza el SQL y limpia la cache antes de volver a consultar.
        em.flush();
        em.clear();

        long cantidadDespues = em.createQuery(
                "select count(c) from Cita c", Long.class
        ).getSingleResult();

        Cita guardada = em.createQuery("""
                        select c from Cita c
                        where c.paciente.usuario.email = :email
                          and c.motivo = :motivo
                        """, Cita.class)
                .setParameter("email", usuarioPaciente.getEmail())
                .setParameter(
                        "motivo", "Prueba de integracion de reserva"
                )
                .getSingleResult();

        assertEquals(cantidadAntes + 1, cantidadDespues);
        assertEquals(EstadoCita.PENDIENTE, guardada.getEstado());
        assertEquals(fecha.atTime(hora), guardada.getFechaHoraInicio());
        assertEquals(
                fecha.atTime(hora).plusMinutes(30),
                guardada.getFechaHoraFin()
        );
        assertEquals(medico.getId(), guardada.getMedico().getId());
        assertEquals(paciente.getId(), guardada.getPaciente().getId());
        assertEquals(
                consultorio.getId(), guardada.getConsultorio().getId()
        );
    }

    private Usuario crearUsuario(
            String nombres,
            String apellidos,
            String dni,
            String email,
            Rol rol
    ) {
        Usuario usuario = new Usuario();
        usuario.setNombres(nombres);
        usuario.setApellidos(apellidos);
        usuario.setDni(dni);
        usuario.setEmail(email);
        usuario.setTelefono("900000001");
        usuario.setPassword("valor-ficticio-no-usado-para-login");
        usuario.setRol(rol);
        usuario.setActivo(true);
        em.persist(usuario);
        return usuario;
    }
}