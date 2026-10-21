package com.gamezdev.sicp.programacion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DepartamentoForm {
    private Integer id;

    @NotBlank(message = "La descripción es obligatoria.")
    @Size(max = 100, message = "Máximo 100 caracteres.")
    private String descripcion;
}