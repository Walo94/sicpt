package com.gamezdev.sicp.diseno.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "unidades_medida")
@Getter
@Setter
public class UnidadMedida {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 10)
    private String codigo;

    @Column(nullable = false, length = 50)
    private String descripcion;

    /** Clave oficial del SAT. Opcional. */
    @Column(name = "clave_sat", length = 10)
    private String claveSat;

    /** 1 = Activo, 0 = Inactivo */
    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(nullable = false)
    private Integer estatus = 1;

    public boolean isActivo() {
        return estatus != null && estatus == 1;
    }
}