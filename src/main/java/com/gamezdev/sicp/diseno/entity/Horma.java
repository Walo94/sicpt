package com.gamezdev.sicp.diseno.entity;

import java.math.BigDecimal;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.gamezdev.sicp.catalogos.entity.Talla;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "hormas")
@Getter
@Setter
public class Horma {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "clave_horma", nullable = false, unique = true, length = 50)
    private String claveHorma;

    @Column(nullable = false, length = 100)
    private String descripcion;

    /** Relación con tallas (rango permitido). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_talla_escala", nullable = false, foreignKey = @jakarta.persistence.ForeignKey(name = "fk_hormas_tallas"))
    private Talla tallaEscala;

    @Column(name = "punto_base", nullable = false, precision = 5, scale = 1)
    private BigDecimal puntoBase;

    @Column(name = "altura_tacon_mm", nullable = false)
    private Integer alturaTaconMm = 0;

    /** 1 = Activo, 0 = Inactivo */
    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(nullable = false)
    private Integer estatus = 1;

    public boolean isActivo() {
        return estatus != null && estatus == 1;
    }
}
