package com.gamezdev.sicp.catalogos.controller;

import com.gamezdev.sicp.catalogos.dto.UnidadForm;
import com.gamezdev.sicp.catalogos.entity.UnidadMedida;
import com.gamezdev.sicp.catalogos.service.UnidadService;
import com.gamezdev.sicp.catalogos.service.UnidadService.DuplicadoException;
import com.gamezdev.sicp.common.util.HtmxResponse;
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
@RequestMapping("/catalogos/unidades")
@RequiredArgsConstructor
public class UnidadController {

    private static final String VISTA = "catalogos/unidades";
    private static final String TABLA = "catalogos/unidades-tabla :: tabla";
    private static final String FORM = "catalogos/unidad-form :: formulario";

    private final UnidadService service;

    @GetMapping
    @PreAuthorize("hasAuthority('UNIDADES_VER')")
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
    @PreAuthorize("hasAuthority('UNIDADES_CREAR')")
    public String nuevo(Model model) {
        model.addAttribute("form", new UnidadForm());
        return FORM;
    }

    @GetMapping("/{id}/editar")
    @PreAuthorize("hasAuthority('UNIDADES_EDITAR')")
    public String editar(@PathVariable Integer id, Model model) {
        UnidadMedida u = obtenerOCuatrocientosCuatro(id);
        UnidadForm form = new UnidadForm();
        form.setId(u.getId());
        form.setCodigo(u.getCodigo());
        form.setDescripcion(u.getDescripcion());
        form.setClaveSat(u.getClaveSat());
        model.addAttribute("form", form);
        return FORM;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('UNIDADES_CREAR')")
    public String crear(@Valid @ModelAttribute("form") UnidadForm form, BindingResult br,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            Model model, HttpServletResponse res) {
        form.setId(null);
        if (!br.hasErrors()) {
            try {
                service.crear(form.getCodigo(), form.getDescripcion(), form.getClaveSat());
            } catch (DuplicadoException e) {
                br.rejectValue("codigo", "duplicado", e.getMessage());
            }
        }
        if (br.hasErrors()) return FORM;

        HtmxResponse.exito(res, "#tablaContainer", "Unidad creada correctamente");
        cargarListado(model, q, page, sort, dir);
        return TABLA;
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAuthority('UNIDADES_EDITAR')")
    public String actualizar(@PathVariable Integer id,
            @Valid @ModelAttribute("form") UnidadForm form, BindingResult br,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            Model model, HttpServletResponse res) {
        obtenerOCuatrocientosCuatro(id);
        form.setId(id);
        if (!br.hasErrors()) {
            try {
                service.actualizar(id, form.getCodigo(), form.getDescripcion(), form.getClaveSat());
            } catch (DuplicadoException e) {
                br.rejectValue("codigo", "duplicado", e.getMessage());
            }
        }
        if (br.hasErrors()) return FORM;

        HtmxResponse.exito(res, "#tablaContainer", "Unidad actualizada correctamente");
        cargarListado(model, q, page, sort, dir);
        return TABLA;
    }

    @PostMapping("/{id}/estatus")
    @PreAuthorize("hasAuthority('UNIDADES_ESTATUS')")
    public String estatus(@PathVariable Integer id,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            Model model, HttpServletResponse res) {
        obtenerOCuatrocientosCuatro(id);
        UnidadMedida u = service.cambiarEstatus(id);
        HtmxResponse.toast(res, "Unidad " + (u.isActivo() ? "activada" : "desactivada") + " correctamente");
        cargarListado(model, q, page, sort, dir);
        return TABLA;
    }

    // ---------------- helpers ----------------
    private UnidadMedida obtenerOCuatrocientosCuatro(Integer id) {
        return service.obtener(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private void cargarListado(Model model, String q, int page, String sort, String dir) {
        Page<UnidadMedida> pagina = service.buscar(q, page, sort, dir);
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
        model.addAttribute("baseUrl", "/catalogos/unidades");
        model.addAttribute("qsQ", qsQ);
        model.addAttribute("qsSort", qsSort);
    }
}