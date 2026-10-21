package com.gamezdev.sicp.mp.entity;

import java.math.BigDecimal;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.gamezdev.sicp.diseno.entity.UnidadMedida;

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
@Table(name = "materiales")
@Getter
@Setter
public class Material {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String descripcion;

    /** Unidad de COMPRA. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_unidad_medida", nullable = false,
            foreignKey = @ForeignKey(name = "fk_materiales_unidad_compra"))
    private UnidadMedida unidadCompra;

    /** Unidad de CONSUMO (BOM / explosión). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_unidad_consumo", nullable = false,
            foreignKey = @ForeignKey(name = "fk_materiales_unidad_consumo"))
    private UnidadMedida unidadConsumo;

    /**
     * Cuántas unidades de consumo hay en 1 unidad de compra.
     * Ej: compra=KG, consumo=GR → 1000.
     */
    @Column(name = "factor_conversion", nullable = false, precision = 14, scale = 6)
    private BigDecimal factorConversion;

    /** D = Directo, I = Indirecto */
    @Column(name = "tipo_material", nullable = false, length = 1)
    private String tipoMaterial;

    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(nullable = false)
    private Integer estatus = 1;

    public boolean isActivo() {
        return estatus != null && estatus == 1;
    }

    public String getTipoMaterialTexto() {
        if (tipoMaterial == null) return "";
        return "D".equalsIgnoreCase(tipoMaterial) ? "Directo" : "Indirecto";
    }
}