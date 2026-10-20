package com.gamezdev.sicp.security.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(length = 100)
    private String apellidos;

    @Column(length = 120)
    private String email;

    /** Se enlazará al catálogo de empleados cuando exista. */
    @Column(name = "empleado_id")
    private Integer empleadoId;

    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(nullable = false)
    private Integer estatus = 1;

    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;

    @Column(name = "creado_en", insertable = false, updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", insertable = false, updatable = false)
    private LocalDateTime actualizadoEn;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "usuario_rol", joinColumns = @JoinColumn(name = "usuario_id"), inverseJoinColumns = @JoinColumn(name = "rol_id"))
    private Set<Rol> roles = new HashSet<>();

    public boolean isActivo() {
        return estatus != null && estatus == 1;
    }

    public String getNombreCompleto() {
        return (nombre + " " + (apellidos == null ? "" : apellidos)).trim();
    }

    public String getUltimoAccesoTexto() {
        return ultimoAcceso == null ? "Nunca"
                : ultimoAcceso.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

}
