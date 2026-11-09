package com.clinicaguadalupe.repository;

import com.clinicaguadalupe.entity.Usuario;
import com.clinicaguadalupe.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByDni(String dni);

    boolean existsByEmail(String email);

    boolean existsByDni(String dni);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByDniAndIdNot(String dni, Long id);

    long countByRol(Rol rol);
}