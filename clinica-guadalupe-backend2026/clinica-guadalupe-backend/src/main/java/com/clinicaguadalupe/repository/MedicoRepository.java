package com.clinicaguadalupe.repository;

import com.clinicaguadalupe.entity.Medico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MedicoRepository extends JpaRepository<Medico, Long> {

    boolean existsByCmp(String cmp);

    boolean existsByCmpAndIdNot(String cmp, Long id);

    Optional<Medico> findByUsuarioId(Long usuarioId);

    List<Medico> findByActivoTrueOrderByUsuarioApellidosAsc();

    List<Medico> findAllByOrderByUsuarioApellidosAsc();

    long countByActivoTrue();

}