package com.gamezdev.sicp.diseno.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UnidadForm {

    private Integer id;

    @NotBlank(message = "El código es obligatorio.")
    @Size(max = 10, message = "Máximo 10 caracteres.")
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "El código solo admite letras y números.")
    private String codigo;

    @NotBlank(message = "La descripción es obligatoria.")
    @Size(max = 50, message = "Máximo 50 caracteres.")
    private String descripcion;

    /** Opcional. */
    @Size(max = 10, message = "Máximo 10 caracteres.")
    private String claveSat;
}