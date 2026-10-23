package com.gamezdev.sicp.diseno.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;
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
        @JoinColumn(name = "id_marca", nullable = false)
        private Marca marca;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "id_linea", nullable = false)
        private Linea linea;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "id_horma", nullable = false)
        private Horma horma;

        // --- NUEVOS CAMPOS TÉCNICOS ---
        @Column(name = "material_corte", length = 100)
        private String materialCorte;

        @Column(length = 100)
        private String forro;

        @Column(length = 100)
        private String plantilla;

        @Column(length = 100)
        private String suela;

        @Column(name = "color_principal", length = 100)
        private String colorPrincipal;

        @Column(length = 100)
        private String estilo;

        @Column(length = 150)
        private String cordones;

        @Column(length = 150)
        private String contrafuerte;

        @Column(length = 100)
        private String cuello;

        @Column(length = 100)
        private String lengueta;

        @Column(length = 100)
        private String ojales;

        @Column(name = "vida_util", length = 150)
        private String vidaUtil;

        @Column(name = "tallas_disponibles", length = 50)
        private String tallasDisponibles;

        @Column(columnDefinition = "TEXT")
        private String observaciones;

        @Column(columnDefinition = "TEXT")
        private String recomendaciones;

        @Column(columnDefinition = "TEXT")
        private String almacenamiento;

        @Column(name = "imagen_url", length = 255)
        private String imagenUrl;

        // ------------------------------

        @Column(name = "fecha_creacion", nullable = false, updatable = false, insertable = false)
        private LocalDateTime fechaCreacion;

        @JdbcTypeCode(SqlTypes.TINYINT)
        @Column(nullable = false)
        private Integer estatus = 1;

        @Transient
        private ModeloBom bomActivo;

        public boolean isActivo() {
                return estatus != null && estatus == 1;
        }
}