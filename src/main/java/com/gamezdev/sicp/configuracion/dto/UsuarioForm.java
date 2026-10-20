package com.gamezdev.sicp.configuracion.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class UsuarioForm {
    private Integer id;

    @NotBlank(message = "El usuario es obligatorio.")
    @Size(min = 3, max = 50, message = "Debe tener entre 3 y 50 caracteres.")
    @Pattern(regexp = "^[A-Za-z0-9._-]*$", message = "Solo letras, números, punto, guion y guion bajo.")
    private String username;

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 80, message = "Máximo 80 caracteres.")
    private String nombre;

    @Size(max = 100, message = "Máximo 100 caracteres.")
    private String apellidos;

    @Email(message = "Correo electrónico inválido.")
    @Size(max = 120, message = "Máximo 120 caracteres.")
    private String email;

    /** Obligatoria al crear; al editar solo si se quiere cambiar. */
    private String password;
    private String confirmar;

    private List<Integer> rolIds = new ArrayList<>();
}
