package com.gamezdev.sicp.configuracion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class RolForm {
    private Integer id;

    @NotBlank(message = "El código es obligatorio.")
    @Pattern(regexp = "^[A-Za-z][A-Za-z0-9_]{2,29}$|^$", message = "3 a 30 caracteres: letras, números y guion bajo (empieza con letra).")
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 60, message = "Máximo 60 caracteres.")
    private String nombre;

    @Size(max = 200, message = "Máximo 200 caracteres.")
    private String descripcion;

    private List<Integer> permisoIds = new ArrayList<>();
}
