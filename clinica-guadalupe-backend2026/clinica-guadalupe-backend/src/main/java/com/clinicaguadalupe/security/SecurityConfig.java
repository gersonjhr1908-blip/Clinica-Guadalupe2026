package com.clinicaguadalupe.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter authoritiesConverter =
                new JwtGrantedAuthoritiesConverter();

        authoritiesConverter.setAuthoritiesClaimName("rol");
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter authenticationConverter =
                new JwtAuthenticationConverter();

        authenticationConverter.setJwtGrantedAuthoritiesConverter(
                authoritiesConverter
        );

        return authenticationConverter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter,
            CorsConfigurationSource corsConfigurationSource
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .cors(cors ->
                        cors.configurationSource(
                                corsConfigurationSource
                        )
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // Autenticación y monitoreo
                        .requestMatchers(
                                "/api/auth/**",
                                "/actuator/health"
                        ).permitAll()

                        // Especialidades públicas
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/especialidades"
                        ).permitAll()

                        // Perfil propio para cualquier usuario autenticado
                        .requestMatchers("/api/usuarios/me")
                        .authenticated()

                        // Usuarios: administrador
                        .requestMatchers("/api/usuarios/**")
                        .hasRole("ADMINISTRADOR")

                        .requestMatchers("/api/usuarios/me")
                        .authenticated()

                        // Perfil personal del paciente
                        .requestMatchers("/api/pacientes/me")
                        .hasRole("PACIENTE")

                        // Gestión administrativa de pacientes
                        .requestMatchers("/api/pacientes/**")
                        .hasAnyRole(
                                "ADMINISTRADOR",
                                "RECEPCIONISTA"
                        )

                        // Gestión de especialidades
                        .requestMatchers("/api/especialidades/**")
                        .hasRole("ADMINISTRADOR")

                        // Listado público de médicos
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/medicos",
                                "/api/medicos/*"
                        ).permitAll()

                        // El médico puede actualizar únicamente su propio perfil
                        .requestMatchers(HttpMethod.PUT, "/api/medicos/me")
                        .hasRole("MEDICO")

                        // Gestión de médicos
                        .requestMatchers("/api/medicos/**")
                        .hasRole("ADMINISTRADOR")

                        // Listado público de consultorios
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/consultorios",
                                "/api/consultorios/*"
                        ).permitAll()

                        // Gestión de consultorios
                        .requestMatchers("/api/consultorios/**")
                        .hasRole("ADMINISTRADOR")

                        // Horarios públicos por médico
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/horarios/medico/*"
                        ).permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/horarios/me")
                        .hasRole("MEDICO")

                        // Gestión administrativa de horarios
                        .requestMatchers("/api/horarios/**")
                        .hasRole("ADMINISTRADOR")

                        // Paciente: reservar cita
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/citas"
                        ).hasRole("PACIENTE")

                        // Paciente: listar sus citas
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/citas/mis-citas"
                        ).hasRole("PACIENTE")

                        // Paciente: cancelar una cita
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/citas/*/cancelar"
                        ).hasRole("PACIENTE")

                        // Médico: listar sus citas
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/citas/medico/mis-citas"
                        ).hasRole("MEDICO")

                        // Médico: actualizar estado de su cita
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/citas/medico/*/estado"
                        ).hasRole("MEDICO")

                        // Administración y recepción de citas
                        .requestMatchers("/api/citas/**")
                        .hasAnyRole(
                                "ADMINISTRADOR",
                                "RECEPCIONISTA"
                        )

                        // Paciente: procesar pago
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/pagos"
                        ).hasRole("PACIENTE")

                        // Paciente: listar sus pagos
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/pagos/mis-pagos"
                        ).hasRole("PACIENTE")

                        // Administrador: reembolsar pago
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/pagos/*/reembolsar"
                        ).hasRole("ADMINISTRADOR")

                        // Administración y recepción de pagos
                        .requestMatchers("/api/pagos/**")
                        .hasAnyRole(
                                "ADMINISTRADOR",
                                "RECEPCIONISTA"
                        )

                        // Dashboard administrativo
                        .requestMatchers("/api/dashboard/**")
                        .hasAnyRole(
                                "ADMINISTRADOR",
                                "RECEPCIONISTA"
                        )

                        // Disponibilidad pública
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/disponibilidad/**"
                        ).permitAll()

                        // Cualquier otra ruta necesita autenticación
                        .anyRequest().authenticated()
                )

                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(
                                        jwtAuthenticationConverter
                                )
                        )
                );

        return http.build();
    }
}
