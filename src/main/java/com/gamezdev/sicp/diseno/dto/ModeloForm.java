package com.gamezdev.sicp.diseno.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModeloForm {

    private Integer id;

    @NotBlank(message = "El código del modelo es obligatorio.")
    @Size(max = 50, message = "Máximo 50 caracteres.")
    private String codigoModelo;

    @NotBlank(message = "La descripción es obligatoria.")
    @Size(max = 150, message = "Máximo 150 caracteres.")
    private String descripcion;

    @NotNull(message = "Debes seleccionar una marca.")
    private Integer marcaId;
    private String marcaTexto;

    @NotNull(message = "Debes seleccionar una línea.")
    private Integer lineaId;
    private String lineaTexto;

    @NotNull(message = "Debes seleccionar una horma.")
    private Integer hormaId;
    private String hormaTexto;
}