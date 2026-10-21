package com.gamezdev.sicp.diseno.entity;

import java.math.BigDecimal;

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
@Table(name = "tallas")
@Getter
@Setter
public class Talla {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 100)
    private String descripcion;

    @Column(name = "punto_inicial", nullable = false, precision = 5, scale = 1)
    private BigDecimal puntoInicial;

    @Column(name = "punto_final", nullable = false, precision = 5, scale = 1)
    private BigDecimal puntoFinal;

    /** 1 = Activo, 0 = Inactivo */
    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(nullable = false)
    private Integer estatus = 1;

    public boolean isActivo() {
        return estatus != null && estatus == 1;
    }
}