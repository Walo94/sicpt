package com.gamezdev.sicp.diseno.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "modelos")
@Getter
@Setter
public class Modelo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "codigo_modelo", nullable = false, unique = true, length = 50)
    private String codigoModelo;

    @Column(nullable = false, length = 150)
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_marca", nullable = false,
            foreignKey = @ForeignKey(name = "fk_modelos_marcas"))
    private Marca marca;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_linea", nullable = false,
            foreignKey = @ForeignKey(name = "fk_modelos_lineas"))
    private Linea linea;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_horma", nullable = false,
            foreignKey = @ForeignKey(name = "fk_modelos_hormas"))
    private Horma horma;

    /** La BD lo llena con DEFAULT CURRENT_TIMESTAMP. */
    @Column(name = "fecha_creacion", nullable = false, updatable = false,
            insertable = false)
    private LocalDateTime fechaCreacion;

    /** 1 = Activo, 0 = Inactivo */
    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(nullable = false)
    private Integer estatus = 1;

    public boolean isActivo() {
        return estatus != null && estatus == 1;
    }
}