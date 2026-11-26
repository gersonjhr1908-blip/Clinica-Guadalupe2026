package com.clinicaguadalupe.repository;

import com.clinicaguadalupe.entity.Cita;
import com.clinicaguadalupe.entity.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CitaRepository extends JpaRepository<Cita, Long> {

    List<Cita> findByPacienteUsuarioEmailOrderByFechaHoraInicioDesc(
            String email
    );

    List<Cita> findAllByOrderByFechaHoraInicioDesc();

    @Query("""
            SELECT COUNT(c)
            FROM Cita c
            WHERE c.medico.id = :medicoId
              AND c.estado <> :estadoCancelada
              AND c.fechaHoraInicio < :fin
              AND c.fechaHoraFin > :inicio
            """)
    long contarCrucesMedico(
            @Param("medicoId") Long medicoId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("estadoCancelada") EstadoCita estadoCancelada
    );

    @Query("""
            SELECT COUNT(c)
            FROM Cita c
            WHERE c.paciente.id = :pacienteId
              AND c.estado <> :estadoCancelada
              AND c.fechaHoraInicio < :fin
              AND c.fechaHoraFin > :inicio
            """)
    long contarCrucesPaciente(
            @Param("pacienteId") Long pacienteId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("estadoCancelada") EstadoCita estadoCancelada
    );

    @Query("""
            SELECT COUNT(c)
            FROM Cita c
            WHERE c.consultorio.id = :consultorioId
              AND c.estado <> :estadoCancelada
              AND c.fechaHoraInicio < :fin
              AND c.fechaHoraFin > :inicio
            """)
    long contarCrucesConsultorio(
            @Param("consultorioId") Long consultorioId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("estadoCancelada") EstadoCita estadoCancelada
    );

    Optional<Cita> findByIdAndPacienteUsuarioEmail(
            Long id,
            String email
    );

    List<Cita> findByMedicoUsuarioEmailOrderByFechaHoraInicioAsc(
            String email
    );

    Optional<Cita> findByIdAndMedicoUsuarioEmail(
            Long id,
            String email
    );

    boolean existsByConsultorioId(Long consultorioId);

    boolean existsByPacienteUsuarioId(Long usuarioId);

    boolean existsByMedicoUsuarioId(Long usuarioId);

    long countByEstado(EstadoCita estado);

    long countByFechaHoraInicioGreaterThanEqualAndFechaHoraInicioLessThan(
            LocalDateTime inicio,
            LocalDateTime fin
    );

    @Query("""
        SELECT c
        FROM Cita c
        WHERE c.medico.id = :medicoId
          AND c.fechaHoraInicio >= :inicio
          AND c.fechaHoraInicio < :fin
          AND c.estado <> :estadoCancelada
        ORDER BY c.fechaHoraInicio
        """)
    List<Cita> buscarCitasActivasDelMedico(
            @Param("medicoId") Long medicoId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("estadoCancelada") EstadoCita estadoCancelada
    );
}
