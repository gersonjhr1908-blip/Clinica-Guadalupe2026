package com.clinicaguadalupe.dto;

import com.clinicaguadalupe.entity.Sexo;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PacienteResponse(

        Long id,
        Long usuarioId,
        String nombres,
        String apellidos,
        String dni,
        String email,
        String telefono,
        LocalDate fechaNacimiento,
        Sexo sexo,
        String direccion,
        String tipoSangre,
        String alergias,
        String contactoEmergencia,
        String telefonoEmergencia,
        LocalDateTime fechaActualizacion

) {
}