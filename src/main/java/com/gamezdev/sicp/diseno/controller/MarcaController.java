package com.gamezdev.sicp.diseno.controller;

import com.gamezdev.sicp.common.dto.OpcionDto;
import com.gamezdev.sicp.common.util.HtmxResponse;
import com.gamezdev.sicp.diseno.dto.MarcaForm;
import com.gamezdev.sicp.diseno.entity.Marca;
import com.gamezdev.sicp.diseno.service.MarcaService;
import com.gamezdev.sicp.diseno.service.MarcaService.DuplicadoException;

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
@RequestMapping("/diseno/marcas")
@RequiredArgsConstructor
public class MarcaController {

    private static final String VISTA = "diseno/marcas";
    private static final String TABLA = "diseno/marcas-tabla :: tabla";
    private static final String FORM = "diseno/marca-form :: formulario";

    private final MarcaService service;

    // ---------------- LISTADO (página completa o fragmento HTMX) ----------------
    @GetMapping
    @PreAuthorize("hasAuthority('MARCAS_VER')")
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
    @PreAuthorize("hasAuthority('MARCAS_CREAR')")
    public String nuevo(Model model) {
        model.addAttribute("form", new MarcaForm());
        return FORM;
    }

    @GetMapping("/{id}/editar")
    @PreAuthorize("hasAuthority('MARCAS_EDITAR')")
    public String editar(@PathVariable Integer id, Model model) {
        Marca m = obtenerOCuatrocientosCuatro(id);
        MarcaForm form = new MarcaForm();
        form.setId(m.getId());
        form.setDescripcion(m.getDescripcion());
        model.addAttribute("form", form);
        return FORM;
    }

    // ---------------- GUARDAR ----------------
    @PostMapping
    @PreAuthorize("hasAuthority('MARCAS_CREAR')")
    public String crear(@Valid @ModelAttribute("form") MarcaForm form, BindingResult br,
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

        HtmxResponse.exito(res, "#tablaContainer", "Marca creada correctamente");
        cargarListado(model, q, page, sort, dir);
        return TABLA;
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAuthority('MARCAS_EDITAR')")
    public String actualizar(@PathVariable Integer id,
            @Valid @ModelAttribute("form") MarcaForm form, BindingResult br,
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

        HtmxResponse.exito(res, "#tablaContainer", "Marca actualizada correctamente");
        cargarListado(model, q, page, sort, dir);
        return TABLA;
    }

    // ---------------- ALTA / BAJA ----------------
    @PostMapping("/{id}/estatus")
    @PreAuthorize("hasAuthority('MARCAS_ESTATUS')")
    public String estatus(@PathVariable Integer id,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            Model model, HttpServletResponse res) {
        obtenerOCuatrocientosCuatro(id);
        Marca m = service.cambiarEstatus(id);
        HtmxResponse.toast(res, "Marca " + (m.isActivo() ? "activado" : "desactivado") + " correctamente");
        cargarListado(model, q, page, sort, dir);
        return TABLA;
    }

    // ---------------- helpers ----------------
    private Marca obtenerOCuatrocientosCuatro(Integer id) {
        return service.obtener(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private void cargarListado(Model model, String q, int page, String sort, String dir) {
        Page<Marca> pagina = service.buscar(q, page, sort, dir);
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
        model.addAttribute("baseUrl", "/diseno/marcas");
        model.addAttribute("qsQ", qsQ); // q=...
        model.addAttribute("qsSort", qsSort); // q=...&sort=...&dir=...
    }

    @GetMapping("/opciones")
    @PreAuthorize("hasAnyAuthority('MODELOS_VER','MODELOS_CREAR','MODELOS_EDITAR')")
    @ResponseBody
    public java.util.List<OpcionDto> opciones(
            @RequestParam(defaultValue = "") String q) {
        return service.buscarActivasParaCombo(q);
    }

}
