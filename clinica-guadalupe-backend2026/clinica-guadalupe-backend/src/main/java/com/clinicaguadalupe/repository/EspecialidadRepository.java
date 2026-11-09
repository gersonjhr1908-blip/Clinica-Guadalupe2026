package com.clinicaguadalupe.repository;

import com.clinicaguadalupe.entity.Especialidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EspecialidadRepository
        extends JpaRepository<Especialidad, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(
            String nombre,
            Long id
    );

    List<Especialidad> findByActivoTrueOrderByNombreAsc();

    List<Especialidad> findAllByOrderByNombreAsc();

    long countByActivoTrue();
}