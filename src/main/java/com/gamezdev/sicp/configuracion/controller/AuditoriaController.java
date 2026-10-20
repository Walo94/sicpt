package com.gamezdev.sicp.configuracion.controller;

import com.gamezdev.sicp.configuracion.service.AuditoriaConsultaService;
import com.gamezdev.sicp.security.entity.Auditoria;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;

/** Consulta de solo lectura de la bitácora de auditoría. */
@Controller
@RequestMapping("/configuracion/auditoria")
@RequiredArgsConstructor
public class AuditoriaController {

    private static final String URL = "/configuracion/auditoria";

    private final AuditoriaConsultaService service;

    @GetMapping
    @PreAuthorize("hasAuthority('AUDITORIA_VER')")
    public String listar(@RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "") String accion,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestHeader(value = "HX-Request", required = false) String hx,
            Model model) {

        Page<Auditoria> pagina = service.buscar(q, accion, desde, hasta, page);
        if (pagina.getTotalPages() > 0 && page >= pagina.getTotalPages()) {
            pagina = service.buscar(q, accion, desde, hasta, pagina.getTotalPages() - 1);
        }

        model.addAttribute("pagina", pagina);
        model.addAttribute("baseUrl", URL);
        model.addAttribute("qs", UriComponentsBuilder.newInstance()
                .queryParam("q", q)
                .queryParam("accion", accion)
                .queryParam("desde", desde == null ? "" : desde.toString())
                .queryParam("hasta", hasta == null ? "" : hasta.toString())
                .build().encode().getQuery());

        if (hx != null)
            return "configuracion/auditoria-tabla :: tabla";

        model.addAttribute("q", q);
        model.addAttribute("accion", accion);
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        model.addAttribute("acciones", service.acciones());
        return "configuracion/auditoria";
    }
}
