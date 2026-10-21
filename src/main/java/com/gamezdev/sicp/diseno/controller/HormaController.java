package com.gamezdev.sicp.diseno.controller;

import com.gamezdev.sicp.common.dto.OpcionDto;
import com.gamezdev.sicp.common.util.HtmxResponse;
import com.gamezdev.sicp.diseno.dto.HormaForm;
import com.gamezdev.sicp.diseno.entity.Horma;
import com.gamezdev.sicp.diseno.service.HormaService;
import com.gamezdev.sicp.diseno.service.TallaService;
import com.gamezdev.sicp.diseno.service.HormaService.DuplicadoException;
import com.gamezdev.sicp.diseno.service.HormaService.TallaInvalidaException;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
@RequestMapping("/diseno/hormas")
@RequiredArgsConstructor
public class HormaController {
    private static final String VISTA = "diseno/hormas";
    private static final String TABLA = "diseno/hormas-tabla :: tabla";
    private static final String FORM = "diseno/horma-form :: formulario";

    private final HormaService service;
    private final TallaService tallaService;

    @GetMapping
    @PreAuthorize("hasAuthority('HORMAS_VER')")
    public String listar(@RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            @RequestHeader(value = "HX-Request", required = false) String hx,
            Model model) {
        cargarListado(model, q, page, sort, dir);
        return hx != null ? TABLA : VISTA;
    }

    @GetMapping("/nuevo")
    @PreAuthorize("hasAuthority('HORMAS_CREAR')")
    public String nuevo(Model model) {
        model.addAttribute("form", new HormaForm());
        return FORM;
    }

    @GetMapping("/{id}/editar")
    @PreAuthorize("hasAuthority('HORMAS_EDITAR')")
    public String editar(@PathVariable Integer id, Model model) {
        Horma h = obtenerOCuatrocientosCuatro(id);
        HormaForm form = new HormaForm();
        form.setId(h.getId());
        form.setClaveHorma(h.getClaveHorma());
        form.setDescripcion(h.getDescripcion());
        form.setTallaEscalaId(h.getTallaEscala().getId());
        form.setTallaEscalaTexto(h.getTallaEscala().getDescripcion());
        form.setPuntoBase(h.getPuntoBase());
        form.setAlturaTaconMm(h.getAlturaTaconMm());
        model.addAttribute("form", form);
        return FORM;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('HORMAS_CREAR')")
    public String crear(@Valid @ModelAttribute("form") HormaForm form, BindingResult br,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            Model model, HttpServletResponse res) {
        form.setId(null);
        if (!br.hasErrors()) {
            try {
                service.crear(form.getClaveHorma(), form.getDescripcion(),
                        form.getTallaEscalaId(), form.getPuntoBase(), form.getAlturaTaconMm());
            } catch (DuplicadoException e) {
                br.rejectValue("claveHorma", "duplicado", e.getMessage());
            } catch (TallaInvalidaException e) {
                br.rejectValue("tallaEscalaId", "tallaInvalida", e.getMessage());
            }
        }
        if (br.hasErrors()) {
            // Refrescar label del combo si vino con datos
            if (form.getTallaEscalaId() != null) {
                tallaService.obtener(form.getTallaEscalaId())
                        .ifPresent(t -> form.setTallaEscalaTexto(t.getDescripcion()));
            }
            return FORM;
        }

        HtmxResponse.exito(res, "#tablaContainer", "Horma creada correctamente");
        cargarListado(model, q, page, sort, dir);
        return TABLA;
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAuthority('HORMAS_EDITAR')")
    public String actualizar(@PathVariable Integer id,
            @Valid @ModelAttribute("form") HormaForm form, BindingResult br,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            Model model, HttpServletResponse res) {
        obtenerOCuatrocientosCuatro(id);
        form.setId(id);
        if (!br.hasErrors()) {
            try {
                service.actualizar(id, form.getClaveHorma(), form.getDescripcion(),
                        form.getTallaEscalaId(), form.getPuntoBase(), form.getAlturaTaconMm());
            } catch (DuplicadoException e) {
                br.rejectValue("claveHorma", "duplicado", e.getMessage());
            } catch (TallaInvalidaException e) {
                br.rejectValue("tallaEscalaId", "tallaInvalida", e.getMessage());
            }
        }
        if (br.hasErrors()) {
            if (form.getTallaEscalaId() != null) {
                tallaService.obtener(form.getTallaEscalaId())
                        .ifPresent(t -> form.setTallaEscalaTexto(t.getDescripcion()));
            }
            return FORM;
        }

        HtmxResponse.exito(res, "#tablaContainer", "Horma actualizada correctamente");
        cargarListado(model, q, page, sort, dir);
        return TABLA;
    }

    @PostMapping("/{id}/estatus")
    @PreAuthorize("hasAuthority('HORMAS_ESTATUS')")
    public String estatus(@PathVariable Integer id,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            Model model, HttpServletResponse res) {
        obtenerOCuatrocientosCuatro(id);
        Horma h = service.cambiarEstatus(id);
        HtmxResponse.toast(res, "Horma " + (h.isActivo() ? "activada" : "desactivada") + " correctamente");
        cargarListado(model, q, page, sort, dir);
        return TABLA;
    }

    // ---------------- helpers ----------------
    private Horma obtenerOCuatrocientosCuatro(Integer id) {
        return service.obtener(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private void cargarListado(Model model, String q, int page, String sort, String dir) {
        Page<Horma> pagina = service.buscar(q, page, sort, dir);
        if (pagina.getTotalPages() > 0 && page >= pagina.getTotalPages()) {
            pagina = service.buscar(q, pagina.getTotalPages() - 1, sort, dir);
        }
        String campo = pagina.getSort().stream().findFirst().map(o -> o.getProperty()).orElse("id");
        String direccion = pagina.getSort().stream().findFirst().map(o -> o.isAscending() ? "asc" : "desc")
                .orElse("asc");

        String qsQ = UriComponentsBuilder.newInstance().queryParam("q", q).build().encode().getQuery();
        String qsSort = UriComponentsBuilder.newInstance()
                .queryParam("q", q).queryParam("sort", campo).queryParam("dir", direccion)
                .build().encode().getQuery();

        model.addAttribute("pagina", pagina);
        model.addAttribute("q", q);
        model.addAttribute("sort", campo);
        model.addAttribute("dir", direccion);
        model.addAttribute("baseUrl", "/diseno/hormas");
        model.addAttribute("qsQ", qsQ);
        model.addAttribute("qsSort", qsSort);
    }

    @GetMapping("/opciones")
    @PreAuthorize("hasAnyAuthority('MODELOS_VER','MODELOS_CREAR','MODELOS_EDITAR','HORMAS_VER','HORMAS_CREAR','HORMAS_EDITAR')")
    @ResponseBody
    public List<OpcionDto> opciones(@RequestParam(defaultValue = "") String q) {
        return service.buscarActivasParaCombo(q);
    }
}
