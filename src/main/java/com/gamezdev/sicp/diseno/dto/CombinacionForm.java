package com.gamezdev.sicp.diseno.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CombinacionForm {

    private Integer id;

    @NotBlank(message = "El código de la combinación es obligatorio.")
    @Size(max = 100, message = "Máximo 100 caracteres.")
    private String codigoCombinacion;

    @NotNull(message = "Debes seleccionar un modelo.")
    private Integer modeloId;
    private String modeloTexto; // Auxiliar para el combo visual

    @NotNull(message = "Debes seleccionar un color.")
    private Integer colorId;
    private String colorTexto;

    @NotNull(message = "Debes seleccionar una talla.")
    private Integer tallaId;
    private String tallaTexto;

    @NotNull(message = "Debes seleccionar una unidad de venta.")
    private Integer unidadVentaId;
    private String unidadVentaTexto;

    @NotBlank(message = "La clave SAT es obligatoria.")
    @Size(max = 15, message = "Máximo 15 caracteres.")
    private String claveSatProducto;

    @NotNull(message = "El precio base es obligatorio.")
    @DecimalMin(value = "0.00", message = "El precio no puede ser negativo.")
    private BigDecimal precioVentaBase;
}