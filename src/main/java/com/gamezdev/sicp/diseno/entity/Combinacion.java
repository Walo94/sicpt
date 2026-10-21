package com.gamezdev.sicp.diseno.entity;

import java.math.BigDecimal;

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
@Table(name = "productos_combinaciones")
@Getter
@Setter
public class Combinacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "codigo_combinacion", nullable = false, unique = true, length = 100)
    private String codigoCombinacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_modelo", nullable = false,
            foreignKey = @ForeignKey(name = "fk_comb_modelo"))
    private Modelo modelo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_color", nullable = false,
            foreignKey = @ForeignKey(name = "fk_comb_color"))
    private Color color;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_talla", nullable = false,
            foreignKey = @ForeignKey(name = "fk_comb_talla"))
    private Talla talla;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_unidad_venta", nullable = false,
            foreignKey = @ForeignKey(name = "fk_comb_unidad"))
    private UnidadMedida unidadVenta;

    @Column(name = "clave_sat_producto", nullable = false, length = 15)
    private String claveSatProducto;

    @Column(name = "precio_venta_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioVentaBase;

    @JdbcTypeCode(SqlTypes.TINYINT)
    @Column(nullable = false)
    private Integer estatus = 1;

    public boolean isActivo() {
        return estatus != null && estatus == 1;
    }
}