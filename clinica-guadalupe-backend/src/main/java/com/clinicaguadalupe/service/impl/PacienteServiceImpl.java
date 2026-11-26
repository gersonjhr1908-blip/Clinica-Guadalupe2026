package com.clinicaguadalupe.service.impl;

import com.clinicaguadalupe.dto.PacienteActualizarRequest;
import com.clinicaguadalupe.dto.PacienteResponse;
import com.clinicaguadalupe.entity.Paciente;
import com.clinicaguadalupe.entity.Rol;
import com.clinicaguadalupe.entity.Usuario;
import com.clinicaguadalupe.repository.PacienteRepository;
import com.clinicaguadalupe.repository.UsuarioRepository;
import com.clinicaguadalupe.service.PacienteService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PacienteServiceImpl implements PacienteService {

    private final PacienteRepository pacienteRepository;
    private final UsuarioRepository usuarioRepository;

    public PacienteServiceImpl(
            PacienteRepository pacienteRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.pacienteRepository = pacienteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional
    public PacienteResponse obtenerMiPerfil(Long usuarioId) {
        Paciente paciente = buscarMiPerfil(usuarioId);
        return convertirRespuesta(paciente);
    }

    @Override
    @Transactional
    public PacienteResponse actualizarMiPerfil(
            Long usuarioId,
            PacienteActualizarRequest request
    ) {
        Paciente paciente = buscarMiPerfil(usuarioId);

        paciente.setFechaNacimiento(request.fechaNacimiento());
        paciente.setSexo(request.sexo());
        paciente.setDireccion(request.direccion());
        paciente.setTipoSangre(request.tipoSangre());
        paciente.setAlergias(request.alergias());
        paciente.setContactoEmergencia(request.contactoEmergencia());
        paciente.setTelefonoEmergencia(request.telefonoEmergencia());

        Paciente actualizado = pacienteRepository.saveAndFlush(paciente);

        return convertirRespuesta(actualizado);
    }

    @Override
    @Transactional
    public Paciente obtenerOCrearPorEmail(String email) {
        Usuario usuario = usuarioRepository.bloquearPorEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "El usuario del token ya no existe"
                ));

        return obtenerOCrearPerfil(usuario);
    }

    private Paciente buscarMiPerfil(Long usuarioId) {
        Usuario usuario = usuarioRepository.bloquearPorId(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "El usuario del token ya no existe"
                ));

        return obtenerOCrearPerfil(usuario);
    }

    private Paciente obtenerOCrearPerfil(Usuario usuario) {
        if (!Boolean.TRUE.equals(usuario.getActivo())
                || usuario.getRol() != Rol.PACIENTE) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Se requiere una cuenta de paciente activa"
            );
        }

        return pacienteRepository.bloquearPorUsuarioId(usuario.getId()).orElseGet(() -> {
            Paciente nuevo = new Paciente();
            nuevo.setUsuario(usuario);
            return pacienteRepository.save(nuevo);
        });
    }

    private PacienteResponse convertirRespuesta(Paciente paciente) {
        Usuario usuario = paciente.getUsuario();

        return new PacienteResponse(
                paciente.getId(),
                usuario.getId(),
                usuario.getNombres(),
                usuario.getApellidos(),
                usuario.getDni(),
                usuario.getEmail(),
                usuario.getTelefono(),
                paciente.getFechaNacimiento(),
                paciente.getSexo(),
                paciente.getDireccion(),
                paciente.getTipoSangre(),
                paciente.getAlergias(),
                paciente.getContactoEmergencia(),
                paciente.getTelefonoEmergencia(),
                paciente.getFechaActualizacion()
        );
    }
}
