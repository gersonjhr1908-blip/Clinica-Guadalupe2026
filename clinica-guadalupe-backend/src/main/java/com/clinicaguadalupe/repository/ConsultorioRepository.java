package com.clinicaguadalupe.repository;

import com.clinicaguadalupe.entity.Consultorio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConsultorioRepository
        extends JpaRepository<Consultorio, Long> {

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNot(
            String codigo,
            Long id
    );

    List<Consultorio> findByActivoTrueOrderByCodigoAsc();

    List<Consultorio> findAllByOrderByCodigoAsc();

    long countByActivoTrue();
}