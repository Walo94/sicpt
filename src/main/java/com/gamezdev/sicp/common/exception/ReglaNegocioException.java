package com.gamezdev.sicp.common.exception;

import org.springframework.validation.BindingResult;

/**
 * Regla de negocio incumplida. Si trae "campo", el error se muestra junto a ese
 * campo del formulario.
 */
public class ReglaNegocioException extends RuntimeException {
    private final String campo;

    public ReglaNegocioException(String mensaje) {
        this(null, mensaje);
    }

    public ReglaNegocioException(String campo, String mensaje) {
        super(mensaje);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }

    public void aplicarA(BindingResult br) {
        if (campo != null)
            br.rejectValue(campo, "regla", getMessage());
        else
            br.reject("regla", getMessage());
    }
}
