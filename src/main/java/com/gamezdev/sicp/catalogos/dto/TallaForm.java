package com.gamezdev.sicp.catalogos.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TallaForm {

    private Integer id;

    @NotBlank(message = "La descripción es obligatoria.")
    @Size(max = 100, message = "Máximo 100 caracteres.")
    private String descripcion;

    @NotNull(message = "El punto inicial es obligatorio.")
    @DecimalMin(value = "0.0", message = "El punto inicial debe ser positivo.")
    private BigDecimal puntoInicial;

    @NotNull(message = "El punto final es obligatorio.")
    @DecimalMin(value = "0.0", message = "El punto final debe ser positivo.")
    private BigDecimal puntoFinal;
}