package com.clinicaguadalupe.repository;

import com.clinicaguadalupe.entity.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PacienteRepository
        extends JpaRepository<Paciente, Long> {

    Optional<Paciente> findByUsuarioId(Long usuarioId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Paciente p where p.usuario.id = :usuarioId")
    Optional<Paciente> bloquearPorUsuarioId(@Param("usuarioId") Long usuarioId);

    boolean existsByUsuarioId(Long usuarioId);

    Optional<Paciente> findByUsuarioEmail(String email);
}