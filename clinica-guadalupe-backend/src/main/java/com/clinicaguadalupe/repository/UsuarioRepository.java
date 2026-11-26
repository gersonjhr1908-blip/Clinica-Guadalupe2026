package com.clinicaguadalupe.repository;

import com.clinicaguadalupe.entity.Usuario;
import com.clinicaguadalupe.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Usuario u where u.id = :id")
    Optional<Usuario> bloquearPorId(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Usuario u where u.email = :email")
    Optional<Usuario> bloquearPorEmail(@Param("email") String email);

    Optional<Usuario> findByDni(String dni);

    boolean existsByEmail(String email);

    boolean existsByDni(String dni);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByDniAndIdNot(String dni, Long id);

    long countByRol(Rol rol);
}