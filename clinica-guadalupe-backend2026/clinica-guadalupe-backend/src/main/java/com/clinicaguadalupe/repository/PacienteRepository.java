package com.clinicaguadalupe.repository;

import com.clinicaguadalupe.entity.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PacienteRepository
        extends JpaRepository<Paciente, Long> {

    Optional<Paciente> findByUsuarioId(Long usuarioId);

    boolean existsByUsuarioId(Long usuarioId);

    Optional<Paciente> findByUsuarioEmail(String email);
}