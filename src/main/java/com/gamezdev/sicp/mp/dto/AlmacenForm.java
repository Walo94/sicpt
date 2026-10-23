package com.gamezdev.sicp.mp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AlmacenForm {

    private Integer id;

    @NotBlank(message = "El código es obligatorio.")
    @Size(max = 20, message = "Máximo 20 caracteres.")
    private String codigo;

    @NotBlank(message = "La descripción es obligatoria.")
    @Size(max = 150, message = "Máximo 150 caracteres.")
    private String descripcion;
}