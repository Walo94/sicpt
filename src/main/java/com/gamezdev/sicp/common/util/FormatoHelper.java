package com.gamezdev.sicp.common.util;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component("fmt")
public class FormatoHelper {

    /** Muestra 22 -> "22", 22.5 -> "22.5", 22.50 -> "22.5". */
    public String punto(BigDecimal v) {
        if (v == null) return "";
        BigDecimal normalizado = v.stripTrailingZeros();
        if (normalizado.scale() <= 0) {
            return normalizado.setScale(0, RoundingMode.UNNECESSARY).toPlainString();
        }
        return normalizado.toPlainString();
    }
}