package com.clinicaguadalupe.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "medicos",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "usuario_id"),
                @UniqueConstraint(columnNames = "cmp")
        }
)
public class Medico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "especialidad_id", nullable = false)
    private Especialidad especialidad;

    @Column(nullable = false, unique = true, length = 20)
    private String cmp;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioConsulta;

    @Column(nullable = false)
    private Integer duracionConsultaMinutos;

    @Column(length = 1000)
    private String biografia;

    @Column(nullable = false)
    private Boolean activo;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false)
    private LocalDateTime fechaActualizacion;

    @PrePersist
    public void prePersist() {
        activo = activo == null ? true : activo;
        fechaCreacion = LocalDateTime.now();
        fechaActualizacion = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        fechaActualizacion = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Especialidad getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(Especialidad especialidad) {
        this.especialidad = especialidad;
    }

    public String getCmp() {
        return cmp;
    }

    public void setCmp(String cmp) {
        this.cmp = cmp;
    }

    public BigDecimal getPrecioConsulta() {
        return precioConsulta;
    }

    public void setPrecioConsulta(BigDecimal precioConsulta) {
        this.precioConsulta = precioConsulta;
    }

    public Integer getDuracionConsultaMinutos() {
        return duracionConsultaMinutos;
    }

    public void setDuracionConsultaMinutos(Integer duracionConsultaMinutos) {
        this.duracionConsultaMinutos = duracionConsultaMinutos;
    }

    public String getBiografia() {
        return biografia;
    }

    public void setBiografia(String biografia) {
        this.biografia = biografia;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }
}