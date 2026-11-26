package com.clinicaguadalupe.repository;

import com.clinicaguadalupe.entity.DiaSemana;
import com.clinicaguadalupe.entity.HorarioMedico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.List;

public interface HorarioMedicoRepository
        extends JpaRepository<HorarioMedico, Long> {

    List<HorarioMedico>
    findByMedicoIdAndActivoTrueOrderByDiaSemanaAscHoraInicioAsc(
            Long medicoId
    );

    List<HorarioMedico>
    findAllByOrderByMedicoUsuarioApellidosAscDiaSemanaAscHoraInicioAsc();

    @Query("""
            SELECT COUNT(h)
            FROM HorarioMedico h
            WHERE h.medico.id = :medicoId
              AND h.diaSemana = :diaSemana
              AND h.activo = true
              AND h.horaInicio < :horaFin
              AND h.horaFin > :horaInicio
              AND (:excluirId IS NULL OR h.id <> :excluirId)
            """)
    long contarCrucesMedico(
            @Param("medicoId") Long medicoId,
            @Param("diaSemana") DiaSemana diaSemana,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin,
            @Param("excluirId") Long excluirId
    );

    @Query("""
            SELECT COUNT(h)
            FROM HorarioMedico h
            WHERE h.consultorio.id = :consultorioId
              AND h.diaSemana = :diaSemana
              AND h.activo = true
              AND h.horaInicio < :horaFin
              AND h.horaFin > :horaInicio
              AND (:excluirId IS NULL OR h.id <> :excluirId)
            """)
    long contarCrucesConsultorio(
            @Param("consultorioId") Long consultorioId,
            @Param("diaSemana") DiaSemana diaSemana,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin,
            @Param("excluirId") Long excluirId
    );

    @Query("""
            SELECT h FROM HorarioMedico h
            WHERE h.medico.id = :medicoId AND h.diaSemana = :diaSemana
              AND h.activo = true AND h.consultorio.activo = true
            ORDER BY h.horaInicio ASC
            """)
    List<HorarioMedico> findByMedicoIdAndDiaSemanaAndActivoTrueOrderByHoraInicioAsc(
            @Param("medicoId") Long medicoId,
            @Param("diaSemana") DiaSemana diaSemana
    );

    boolean existsByConsultorioId(Long consultorioId);

    List<HorarioMedico> findByMedicoId(Long medicoId);
}
