package com.gamezdev.sicp.mp.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MaterialForm {

    private Integer id;

    @NotBlank(message = "El código es obligatorio.")
    @Size(max = 50, message = "Máximo 50 caracteres.")
    private String codigo;

    @NotBlank(message = "La descripción es obligatoria.")
    @Size(max = 150, message = "Máximo 150 caracteres.")
    private String descripcion;

    @NotNull(message = "Debes seleccionar una unidad de compra.")
    private Integer unidadCompraId;

    /** Texto visible en el combo de compra (se llena en el controller al editar). */
    private String unidadCompraTexto;

    @NotNull(message = "Debes seleccionar una unidad de consumo.")
    private Integer unidadConsumoId;

    /** Texto visible en el combo de consumo. */
    private String unidadConsumoTexto;

    @NotNull(message = "El factor de conversión es obligatorio.")
    @DecimalMin(value = "0.000001", inclusive = true,
            message = "El factor debe ser mayor a cero.")
    private BigDecimal factorConversion;

    @NotBlank(message = "El tipo de material es obligatorio.")
    @Pattern(regexp = "^[DI]$", message = "El tipo debe ser D (Directo) o I (Indirecto).")
    private String tipoMaterial;
}