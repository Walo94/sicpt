package com.gamezdev.sicp.programacion.controller;

import com.gamezdev.sicp.common.util.HtmxResponse;
import com.gamezdev.sicp.programacion.dto.DepartamentoForm;
import com.gamezdev.sicp.programacion.entity.Departamento;
import com.gamezdev.sicp.programacion.service.DepartamentoService;
import com.gamezdev.sicp.programacion.service.DepartamentoService.DuplicadoException;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/programacion/departamentos")
@RequiredArgsConstructor
public class DepartamentoController {

    private static final String VISTA = "programacion/departamentos";
    private static final String TABLA = "programacion/departamentos-tabla :: tabla";
    private static final String FORM = "programacion/departamento-form :: formulario";

    private final DepartamentoService service;

    // ---------------- LISTADO (página completa o fragmento HTMX) ----------------
    @GetMapping
    @PreAuthorize("hasAuthority('DEPARTAMENTOS_VER')")
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
    @PreAuthorize("hasAuthority('DEPARTAMENTOS_CREAR')")
    public String nuevo(Model model) {
        model.addAttribute("form", new DepartamentoForm());
        return FORM;
    }

    @GetMapping("/{id}/editar")
    @PreAuthorize("hasAuthority('DEPARTAMENTOS_EDITAR')")
    public String editar(@PathVariable Integer id, Model model) {
        Departamento d = obtenerOCuatrocientosCuatro(id);
        DepartamentoForm form = new DepartamentoForm();
        form.setId(d.getId());
        form.setDescripcion(d.getDescripcion());
        model.addAttribute("form", form);
        return FORM;
    }

    // ---------------- GUARDAR ----------------
    @PostMapping
    @PreAuthorize("hasAuthority('DEPARTAMENTOS_CREAR')")
    public String crear(@Valid @ModelAttribute("form") DepartamentoForm form, BindingResult br,
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

        HtmxResponse.exito(res, "#tablaContainer", "Departamento creado correctamente");
        cargarListado(model, q, page, sort, dir);
        return TABLA;
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAuthority('DEPARTAMENTOS_EDITAR')")
    public String actualizar(@PathVariable Integer id,
            @Valid @ModelAttribute("form") DepartamentoForm form, BindingResult br,
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

        HtmxResponse.exito(res, "#tablaContainer", "Departamento actualizado correctamente");
        cargarListado(model, q, page, sort, dir);
        return TABLA;
    }

    // ---------------- ALTA / BAJA ----------------
    @PostMapping("/{id}/estatus")
    @PreAuthorize("hasAuthority('DEPARTAMENTOS_ESTATUS')")
    public String estatus(@PathVariable Integer id,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            Model model, HttpServletResponse res) {
        obtenerOCuatrocientosCuatro(id);
        Departamento d = service.cambiarEstatus(id);
        HtmxResponse.toast(res, "Departamento " + (d.isActivo() ? "activado" : "desactivado") + " correctamente");
        cargarListado(model, q, page, sort, dir);
        return TABLA;
    }

    // ---------------- helpers ----------------
    private Departamento obtenerOCuatrocientosCuatro(Integer id) {
        return service.obtener(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private void cargarListado(Model model, String q, int page, String sort, String dir) {
        Page<Departamento> pagina = service.buscar(q, page, sort, dir);
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
        model.addAttribute("baseUrl", "/catalogos/departamentos");
        model.addAttribute("qsQ", qsQ); // q=...
        model.addAttribute("qsSort", qsSort); // q=...&sort=...&dir=...
    }
}
