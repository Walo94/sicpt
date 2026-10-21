package com.gamezdev.sicp.diseno.controller;

import com.gamezdev.sicp.common.dto.OpcionDto;
import com.gamezdev.sicp.common.util.HtmxResponse;
import com.gamezdev.sicp.diseno.dto.ColorForm;
import com.gamezdev.sicp.diseno.entity.Color;
import com.gamezdev.sicp.diseno.service.ColorService;
import com.gamezdev.sicp.diseno.service.ColorService.DuplicadoException;
import com.gamezdev.sicp.diseno.service.ColorService.JsonInvalidoException;

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
@RequestMapping("/diseno/colores")
@RequiredArgsConstructor
public class ColorController {

    private static final String VISTA = "diseno/colores";
    private static final String TABLA = "diseno/colores-tabla :: tabla";
    private static final String FORM = "diseno/color-form :: formulario";

    private final ColorService service;

    @GetMapping
    @PreAuthorize("hasAuthority('COLORES_VER')")
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
    @PreAuthorize("hasAuthority('COLORES_CREAR')")
    public String nuevo(Model model) {
        model.addAttribute("form", new ColorForm());
        return FORM;
    }

    @GetMapping("/{id}/editar")
    @PreAuthorize("hasAuthority('COLORES_EDITAR')")
    public String editar(@PathVariable Integer id, Model model) {
        Color c = obtenerOCuatrocientosCuatro(id);
        ColorForm form = new ColorForm();
        form.setId(c.getId());
        form.setDescripcion(c.getDescripcion());
        form.setColorJson(c.getColorJson());
        model.addAttribute("form", form);
        return FORM;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('COLORES_CREAR')")
    public String crear(@Valid @ModelAttribute("form") ColorForm form, BindingResult br,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            Model model, HttpServletResponse res) {
        form.setId(null);
        if (!br.hasErrors()) {
            try {
                service.crear(form.getDescripcion(), form.getColorJson());
            } catch (DuplicadoException e) {
                br.rejectValue("descripcion", "duplicado", e.getMessage());
            } catch (JsonInvalidoException e) {
                br.rejectValue("colorJson", "jsonInvalido", e.getMessage());
            }
        }
        if (br.hasErrors())
            return FORM;

        HtmxResponse.exito(res, "#tablaContainer", "Color creado correctamente");
        cargarListado(model, q, page, sort, dir);
        return TABLA;
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAuthority('COLORES_EDITAR')")
    public String actualizar(@PathVariable Integer id,
            @Valid @ModelAttribute("form") ColorForm form, BindingResult br,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            Model model, HttpServletResponse res) {
        obtenerOCuatrocientosCuatro(id);
        form.setId(id);
        if (!br.hasErrors()) {
            try {
                service.actualizar(id, form.getDescripcion(), form.getColorJson());
            } catch (DuplicadoException e) {
                br.rejectValue("descripcion", "duplicado", e.getMessage());
            } catch (JsonInvalidoException e) {
                br.rejectValue("colorJson", "jsonInvalido", e.getMessage());
            }
        }
        if (br.hasErrors())
            return FORM;

        HtmxResponse.exito(res, "#tablaContainer", "Color actualizado correctamente");
        cargarListado(model, q, page, sort, dir);
        return TABLA;
    }

    @PostMapping("/{id}/estatus")
    @PreAuthorize("hasAuthority('COLORES_ESTATUS')")
    public String estatus(@PathVariable Integer id,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            Model model, HttpServletResponse res) {
        obtenerOCuatrocientosCuatro(id);
        Color c = service.cambiarEstatus(id);
        HtmxResponse.toast(res, "Color " + (c.isActivo() ? "activado" : "desactivado") + " correctamente");
        cargarListado(model, q, page, sort, dir);
        return TABLA;
    }

    // ---------------- helpers ----------------
    private Color obtenerOCuatrocientosCuatro(Integer id) {
        return service.obtener(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private void cargarListado(Model model, String q, int page, String sort, String dir) {
        Page<Color> pagina = service.buscar(q, page, sort, dir);
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
        model.addAttribute("baseUrl", "/diseno/colores");
        model.addAttribute("qsQ", qsQ);
        model.addAttribute("qsSort", qsSort);
    }

    @GetMapping("/opciones")
    @PreAuthorize("hasAnyAuthority('COMBINACIONES_VER','COMBINACIONES_CREAR','COMBINACIONES_EDITAR')")
    @ResponseBody
    public List<OpcionDto> opciones(@RequestParam(defaultValue = "") String q) {
        return service.buscarActivasParaCombo(q);
    }
}