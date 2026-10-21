package com.gamezdev.sicp.diseno.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ColorForm {

    private Integer id;

    @NotBlank(message = "La descripción es obligatoria.")
    @Size(max = 100, message = "Máximo 100 caracteres.")
    private String descripcion;

    /** JSON serializado desde el cliente. Opcional. */
    private String colorJson;

    /** Helper para Thymeleaf: ¿tiene colores configurados? */
    public boolean isTieneColor() {
        return colorJson != null && !colorJson.isBlank();
    }
}