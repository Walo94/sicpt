package com.gamezdev.sicp.common.util;

import jakarta.servlet.http.HttpServletResponse;

public class HtmxResponse {

    private HtmxResponse() {
    }

    /**
     * Reemplaza el contenido de {@code selector} con la respuesta, muestra un toast
     * y cierra el modal.
     */
    public static void exito(HttpServletResponse res, String selector, String mensaje) {
        res.setHeader("HX-Retarget", selector);
        res.setHeader("HX-Reswap", "innerHTML");
        res.setHeader("HX-Trigger",
                "{\"toast\":{\"mensaje\":\"" + esc(mensaje) + "\",\"tipo\":\"success\"},\"cerrarModal\":true}");
    }

    /**
     * Solo muestra un toast verde (la respuesta se inserta en el target original).
     */
    public static void toast(HttpServletResponse res, String mensaje) {
        res.setHeader("HX-Trigger", "{\"toast\":{\"mensaje\":\"" + esc(mensaje) + "\",\"tipo\":\"success\"}}");
    }

    /** Toast rojo y NO modifica la pantalla. */
    public static void error(HttpServletResponse res, String mensaje) {
        res.setHeader("HX-Reswap", "none");
        res.setHeader("HX-Trigger", "{\"toast\":{\"mensaje\":\"" + esc(mensaje) + "\",\"tipo\":\"danger\"}}");
    }

    /**
     * Escapa para JSON y convierte a \\uXXXX todo lo no-ASCII (las cabeceras HTTP
     * deben ser ASCII).
     */
    private static String esc(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c == '"' || c == '\\')
                sb.append('\\').append(c);
            else if (c < 0x20 || c > 0x7e)
                sb.append(String.format("\\u%04x", (int) c));
            else
                sb.append(c);
        }
        return sb.toString();
    }
}
