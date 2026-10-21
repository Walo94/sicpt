package com.gamezdev.sicp.diseno.controller;

import com.gamezdev.sicp.common.dto.OpcionDto;
import com.gamezdev.sicp.common.util.HtmxResponse;
import com.gamezdev.sicp.diseno.dto.LineaForm;
import com.gamezdev.sicp.diseno.entity.Linea;
import com.gamezdev.sicp.diseno.service.LineaService;
import com.gamezdev.sicp.diseno.service.LineaService.DuplicadoException;

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
@RequestMapping("/diseno/lineas")
@RequiredArgsConstructor
public class LineaController {

    private static final String VISTA = "diseno/lineas";
    private static final String TABLA = "diseno/lineas-tabla :: tabla";
    private static final String FORM = "diseno/linea-form :: formulario";

    private final LineaService service;

    // ---------------- LISTADO (página completa o fragmento HTMX) ----------------
    @GetMapping
    @PreAuthorize("hasAuthority('LINEAS_VER')")
    public String listar(@RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            @RequestHeader(value = "HX-Request", required = false) String hx,
            Model model) {
        cargarListado(model, q, page, sort, dir);
        return hx != null ? TABLA : VISTA;
    }

    // ---------------- MODAL: NUEVO / EDITAR ----------------
    @GetMapping("/nuevo")
    @PreAuthorize("hasAuthority('LINEAS_CREAR')")
    public String nuevo(Model model) {
        model.addAttribute("form", new LineaForm());
        return FORM;
    }

    @GetMapping("/{id}/editar")
    @PreAuthorize("hasAuthority('LINEAS_EDITAR')")
    public String editar(@PathVariable Integer id, Model model) {
        Linea m = obtenerOCuatrocientosCuatro(id);
        LineaForm form = new LineaForm();
        form.setId(m.getId());
        form.setDescripcion(m.getDescripcion());
        model.addAttribute("form", form);
        return FORM;
    }

    // ---------------- GUARDAR ----------------
    @PostMapping
    @PreAuthorize("hasAuthority('LINEAS_CREAR')")
    public String crear(@Valid @ModelAttribute("form") LineaForm form, BindingResult br,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            Model model, HttpServletResponse res) {
        form.setId(null);
        if (!br.hasErrors()) {
            try {
                service.crear(form.getDescripcion());
            } catch (DuplicadoException e) {
                br.rejectValue("descripcion", "duplicado", e.getMessage());
            }
        }
        if (br.hasErrors())
            return FORM; // 200 con errores: HTMX lo pinta dentro del modal

        HtmxResponse.exito(res, "#tablaContainer", "Línea creada correctamente");
        cargarListado(model, q, page, sort, dir);
        return TABLA;
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAuthority('LINEAS_EDITAR')")
    public String actualizar(@PathVariable Integer id,
            @Valid @ModelAttribute("form") LineaForm form, BindingResult br,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            Model model, HttpServletResponse res) {
        obtenerOCuatrocientosCuatro(id);
        form.setId(id);
        if (!br.hasErrors()) {
            try {
                service.actualizar(id, form.getDescripcion());
            } catch (DuplicadoException e) {
                br.rejectValue("descripcion", "duplicado", e.getMessage());
            }
        }
        if (br.hasErrors())
            return FORM;

        HtmxResponse.exito(res, "#tablaContainer", "Línea actualizada correctamente");
        cargarListado(model, q, page, sort, dir);
        return TABLA;
    }

    // ---------------- ALTA / BAJA ----------------
    @PostMapping("/{id}/estatus")
    @PreAuthorize("hasAuthority('LINEAS_ESTATUS')")
    public String estatus(@PathVariable Integer id,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            Model model, HttpServletResponse res) {
        obtenerOCuatrocientosCuatro(id);
        Linea m = service.cambiarEstatus(id);
        HtmxResponse.toast(res, "Línea " + (m.isActivo() ? "activada" : "desactivada") + " correctamente");
        cargarListado(model, q, page, sort, dir);
        return TABLA;
    }

    // ---------------- helpers ----------------
    private Linea obtenerOCuatrocientosCuatro(Integer id) {
        return service.obtener(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private void cargarListado(Model model, String q, int page, String sort, String dir) {
        Page<Linea> pagina = service.buscar(q, page, sort, dir);
        // Si se borró/filtró y la página ya no existe, ir a la última
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
        model.addAttribute("baseUrl", "/diseno/lineas");
        model.addAttribute("qsQ", qsQ); // q=...
        model.addAttribute("qsSort", qsSort); // q=...&sort=...&dir=...
    }

    @GetMapping("/opciones")
    @PreAuthorize("hasAnyAuthority('MODELOS_VER','MODELOS_CREAR','MODELOS_EDITAR')")
    @ResponseBody
    public List<OpcionDto> opciones(@RequestParam(defaultValue = "") String q) {
        return service.buscarActivasParaCombo(q);
    }

}
