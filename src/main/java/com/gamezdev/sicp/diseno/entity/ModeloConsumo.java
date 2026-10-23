package com.gamezdev.sicp.diseno.entity;

import java.math.BigDecimal;
import com.gamezdev.sicp.mp.entity.Material;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "modelo_consumos")
@Getter
@Setter
public class ModeloConsumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_bom", nullable = false)
    private ModeloBom bom;

    @Column(name = "pieza_calzado", nullable = false, length = 100)
    private String piezaCalzado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_material", nullable = false)
    private Material material;

    @Column(name = "cantidad_consumo", nullable = false, precision = 10, scale = 4)
    private BigDecimal cantidadConsumo;
}