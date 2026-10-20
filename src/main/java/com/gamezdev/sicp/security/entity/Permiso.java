package com.gamezdev.sicp.security.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "permisos")
@Getter
@Setter
public class Permiso {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Ej. DEPARTAMENTOS_VER. Es el "authority" de Spring Security. */
    @Column(nullable = false, unique = true, length = 60)
    private String codigo;

    @Column(nullable = false, length = 30)
    private String modulo;

    @Column(nullable = false, length = 40)
    private String submodulo;

    @Column(nullable = false, length = 20)
    private String accion;

    @Column(nullable = false, length = 150)
    private String descripcion;
}
