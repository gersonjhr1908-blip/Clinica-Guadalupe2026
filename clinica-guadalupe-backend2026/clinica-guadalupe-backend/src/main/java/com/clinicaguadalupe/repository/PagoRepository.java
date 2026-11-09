package com.clinicaguadalupe.repository;

import com.clinicaguadalupe.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import com.clinicaguadalupe.entity.EstadoPago;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

import java.util.List;
import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    boolean existsByCitaId(Long citaId);

    Optional<Pago> findByCitaId(Long citaId);

    List<Pago> findByCitaPacienteUsuarioEmailOrderByFechaCreacionDesc(
            String email
    );

    List<Pago> findAllByOrderByFechaCreacionDesc();

    long countByEstado(EstadoPago estado);

    @Query("""
        SELECT COALESCE(SUM(p.monto), 0)
        FROM Pago p
        WHERE p.estado = :estado
        """)
    BigDecimal sumarMontoPorEstado(
            @Param("estado") EstadoPago estado
    );
}