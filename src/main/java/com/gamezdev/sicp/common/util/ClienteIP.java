package com.gamezdev.sicp.common.util;

import jakarta.servlet.http.HttpServletRequest;

public class ClienteIP {

    private static final String[] CABECERAS = {
            "X-Forwarded-For",
            "X-Real-IP",
            "CF-Connecting-IP",
            "Forwarded"
    };

    private ClienteIP() {
    }

    public static String resolver(HttpServletRequest request) {
        if (request == null)
            return null;

        for (String cabecera : CABECERAS) {
            String valor = request.getHeader(cabecera);
            if (valor == null || valor.isBlank())
                continue;

            // X-Forwarded-For puede traer "cliente, proxy1, proxy2"
            String ip = valor.split(",")[0].trim();

            // Forwarded: for=192.168.1.1;proto=https
            if (cabecera.equals("Forwarded") && ip.toLowerCase().startsWith("for=")) {
                ip = ip.substring(4).replace("\"", "").trim();
            }

            if (!ip.isEmpty())
                return normalizar(ip);
        }

        return request.getRemoteAddr();
    }

    /** Convierte ::1 y 0:0:0:0:0:0:0:1 en 127.0.0.1 para unificar loopback. */
    private static String normalizar(String ip) {
        if (ip == null)
            return null;
        String limpia = ip.trim();
        if (limpia.equals("::1") || limpia.equals("0:0:0:0:0:0:0:1")) {
            return "127.0.0.1";
        }
        return limpia.length() > 45 ? limpia.substring(0, 45) : limpia;
    }

}
