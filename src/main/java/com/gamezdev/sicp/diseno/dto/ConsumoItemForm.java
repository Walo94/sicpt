package com.gamezdev.sicp.diseno.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConsumoItemForm {
    private Integer materialId;
    private String materialTexto;

    @NotBlank(message = "La pieza es obligatoria.")
    private String piezaCalzado;

    @NotNull(message = "La cantidad es obligatoria.")
    @DecimalMin(value = "0.0001", message = "La cantidad debe ser mayor a 0.")
    private BigDecimal cantidadConsumo;
}