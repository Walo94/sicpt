package com.gamezdev.sicp.mp.controller;

import com.gamezdev.sicp.common.util.HtmxResponse;
import com.gamezdev.sicp.mp.dto.AlmacenForm;
import com.gamezdev.sicp.mp.entity.AlmacenMp;
import com.gamezdev.sicp.mp.service.AlmacenService;
import com.gamezdev.sicp.mp.service.AlmacenService.DuplicadoException;

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
@RequestMapping("/mp/almacenes")
@RequiredArgsConstructor
public class AlmacenController {

    private static final String VISTA = "mp/almacenes";
    private static final String TABLA = "mp/almacenes-tabla :: tabla";
    private static final String FORM = "mp/almacen-form :: formulario";

    private final AlmacenService service;

    @GetMapping
    @PreAuthorize("hasAuthority('ALMACENES_MP_VER')")
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
    @PreAuthorize("hasAuthority('ALMACENES_MP_CREAR')")
    public String nuevo(Model model) {
        model.addAttribute("form", new AlmacenForm());
        return FORM;
    }

    @GetMapping("/{id}/editar")
    @PreAuthorize("hasAuthority('ALMACENES_MP_EDITAR')")
    public String editar(@PathVariable Integer id, Model model) {
        AlmacenMp a = obtener404(id);
        AlmacenForm form = new AlmacenForm();
        form.setId(a.getId());
        form.setCodigo(a.getCodigo());
        form.setDescripcion(a.getDescripcion());
        model.addAttribute("form", form);
        return FORM;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ALMACENES_MP_CREAR')")
    public String crear(@Valid @ModelAttribute("form") AlmacenForm form, BindingResult br,
            @RequestParam(defaultValue = "") String q, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort, @RequestParam(defaultValue = "asc") String dir,
            Model model, HttpServletResponse res) {
        form.setId(null);
        if (!br.hasErrors()) {
            try {
                service.crear(form.getCodigo(), form.getDescripcion());
            } catch (DuplicadoException e) {
                br.rejectValue("codigo", "duplicado", e.getMessage());
            }
        }
        if (br.hasErrors()) return FORM;

        HtmxResponse.exito(res, "#tablaContainer", "Almacén creado correctamente");
        cargarListado(model, q, page, sort, dir);
        return TABLA;
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAuthority('ALMACENES_MP_EDITAR')")
    public String actualizar(@PathVariable Integer id,
            @Valid @ModelAttribute("form") AlmacenForm form, BindingResult br,
            @RequestParam(defaultValue = "") String q, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort, @RequestParam(defaultValue = "asc") String dir,
            Model model, HttpServletResponse res) {
        obtener404(id);
        form.setId(id);
        if (!br.hasErrors()) {
            try {
                service.actualizar(id, form.getCodigo(), form.getDescripcion());
            } catch (DuplicadoException e) {
                br.rejectValue("codigo", "duplicado", e.getMessage());
            }
        }
        if (br.hasErrors()) return FORM;

        HtmxResponse.exito(res, "#tablaContainer", "Almacén actualizado correctamente");
        cargarListado(model, q, page, sort, dir);
        return TABLA;
    }

    @PostMapping("/{id}/estatus")
    @PreAuthorize("hasAuthority('ALMACENES_MP_ESTATUS')")
    public String estatus(@PathVariable Integer id,
            @RequestParam(defaultValue = "") String q, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort, @RequestParam(defaultValue = "asc") String dir,
            Model model, HttpServletResponse res) {
        obtener404(id);
        AlmacenMp a = service.cambiarEstatus(id);
        HtmxResponse.toast(res, "Almacén " + (a.isActivo() ? "activado" : "desactivado") + " correctamente");
        cargarListado(model, q, page, sort, dir);
        return TABLA;
    }

    // ---------------- helpers ----------------
    private AlmacenMp obtener404(Integer id) {
        return service.obtener(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private void cargarListado(Model model, String q, int page, String sort, String dir) {
        Page<AlmacenMp> pagina = service.buscar(q, page, sort, dir);
        if (pagina.getTotalPages() > 0 && page >= pagina.getTotalPages()) {
            pagina = service.buscar(q, pagina.getTotalPages() - 1, sort, dir);
        }
        String campo = pagina.getSort().stream().findFirst().map(o -> o.getProperty()).orElse("id");
        String direccion = pagina.getSort().stream().findFirst().map(o -> o.isAscending() ? "asc" : "desc").orElse("asc");

        String qsQ = UriComponentsBuilder.newInstance().queryParam("q", q).build().encode().getQuery();
        String qsSort = UriComponentsBuilder.newInstance()
                .queryParam("q", q).queryParam("sort", campo).queryParam("dir", direccion)
                .build().encode().getQuery();

        model.addAttribute("pagina", pagina);
        model.addAttribute("q", q);
        model.addAttribute("sort", campo);
        model.addAttribute("dir", direccion);
        model.addAttribute("baseUrl", "/mp/almacenes");
        model.addAttribute("qsQ", qsQ);
        model.addAttribute("qsSort", qsSort);
    }
}