package com.clinicaguadalupe.dto;

import com.clinicaguadalupe.entity.Sexo;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PacienteActualizarRequest(

        @Past(message = "La fecha de nacimiento debe ser anterior a hoy")
        LocalDate fechaNacimiento,

        Sexo sexo,

        @Size(max = 200, message = "La dirección admite hasta 200 caracteres")
        String direccion,

        @Pattern(
                regexp = "^(A|B|AB|O)[+-]$",
                message = "Tipo de sangre inválido. Ejemplos: A+, O-, AB+"
        )
        String tipoSangre,

        @Size(max = 2000, message = "Las alergias admiten hasta 2000 caracteres")
        String alergias,

        @Size(max = 150, message = "El contacto admite hasta 150 caracteres")
        String contactoEmergencia,

        @Pattern(
                regexp = "\\d{9}",
                message = "El teléfono de emergencia debe tener 9 números"
        )
        String telefonoEmergencia

) {
}