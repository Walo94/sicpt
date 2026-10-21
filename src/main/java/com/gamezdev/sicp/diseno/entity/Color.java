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
@Table(name = "colores")
@Getter
@Setter
public class Color {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 100)
    private String descripcion;

    /**
     * JSON con los colores: {"colores":[{"nombre":"Negro","hex":"#000000"}]}. Puede
     * ser null.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "color_json", columnDefinition = "json")
    private String colorJson;

    /** 1 = Activo, 0 = Inactivo */
    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(nullable = false)
    private Integer estatus = 1;

    public boolean isActivo() {
        return estatus != null && estatus == 1;
    }

    public boolean hasColor() {
        return colorJson != null && !colorJson.isBlank();
    }
}