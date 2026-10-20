package com.gamezdev.sicp.diseno.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HormaForm {
    private Integer id;

    @NotBlank(message = "La clave es obligatoria.")
    @Size(max = 50, message = "Máximo 50 caracteres.")
    private String claveHorma;

    @NotBlank(message = "La descripción es obligatoria.")
    @Size(max = 100, message = "Máximo 100 caracteres.")
    private String descripcion;

    @NotNull(message = "Debes seleccionar una talla (rango).")
    private Integer tallaEscalaId;

    @NotNull(message = "El punto base es obligatorio.")
    @DecimalMin(value = "0.0", message = "El punto base debe ser positivo.")
    private BigDecimal puntoBase;

    @NotNull(message = "La altura del tacón es obligatoria.")
    @Min(value = 0, message = "La altura no puede ser negativa.")
    private Integer alturaTaconMm;

    /** Texto visible en el combo (se llena en el controller al editar). */
    private String tallaEscalaTexto;
}
