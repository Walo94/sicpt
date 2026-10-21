package com.gamezdev.sicp.diseno.controller;

import com.gamezdev.sicp.common.dto.OpcionDto;
import com.gamezdev.sicp.common.util.HtmxResponse;
import com.gamezdev.sicp.diseno.dto.TallaForm;
import com.gamezdev.sicp.diseno.entity.Talla;
import com.gamezdev.sicp.diseno.service.TallaService;
import com.gamezdev.sicp.diseno.service.TallaService.DuplicadoException;
import com.gamezdev.sicp.diseno.service.TallaService.RangoInvalidoException;

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
@RequestMapping("/diseno/tallas")
@RequiredArgsConstructor
public class TallaController {

    private static final String VISTA = "diseno/tallas";
    private static final String TABLA = "diseno/tallas-tabla :: tabla";
    private static final String FORM = "diseno/talla-form :: formulario";

    private final TallaService service;

    @GetMapping
    @PreAuthorize("hasAuthority('TALLAS_VER')")
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
    @PreAuthorize("hasAuthority('TALLAS_CREAR')")
    public String nuevo(Model model) {
        model.addAttribute("form", new TallaForm());
        return FORM;
    }

    @GetMapping("/{id}/editar")
    @PreAuthorize("hasAuthority('TALLAS_EDITAR')")
    public String editar(@PathVariable Integer id, Model model) {
        Talla t = obtenerOCuatrocientosCuatro(id);
        TallaForm form = new TallaForm();
        form.setId(t.getId());
        form.setDescripcion(t.getDescripcion());
        form.setPuntoInicial(t.getPuntoInicial());
        form.setPuntoFinal(t.getPuntoFinal());
        model.addAttribute("form", form);
        return FORM;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('TALLAS_CREAR')")
    public String crear(@Valid @ModelAttribute("form") TallaForm form, BindingResult br,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            Model model, HttpServletResponse res) {
        form.setId(null);
        if (!br.hasErrors()) {
            try {
                service.crear(form.getDescripcion(), form.getPuntoInicial(), form.getPuntoFinal());
            } catch (DuplicadoException e) {
                br.rejectValue("descripcion", "duplicado", e.getMessage());
            } catch (RangoInvalidoException e) {
                // Se asigna al campo puntoFinal porque es la regla más relacionada
                br.rejectValue("puntoFinal", "rango", e.getMessage());
            }
        }
        if (br.hasErrors())
            return FORM;

        HtmxResponse.exito(res, "#tablaContainer", "Talla creada correctamente");
        cargarListado(model, q, page, sort, dir);
        return TABLA;
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAuthority('TALLAS_EDITAR')")
    public String actualizar(@PathVariable Integer id,
            @Valid @ModelAttribute("form") TallaForm form, BindingResult br,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            Model model, HttpServletResponse res) {
        obtenerOCuatrocientosCuatro(id);
        form.setId(id);
        if (!br.hasErrors()) {
            try {
                service.actualizar(id, form.getDescripcion(), form.getPuntoInicial(), form.getPuntoFinal());
            } catch (DuplicadoException e) {
                br.rejectValue("descripcion", "duplicado", e.getMessage());
            } catch (RangoInvalidoException e) {
                br.rejectValue("puntoFinal", "rango", e.getMessage());
            }
        }
        if (br.hasErrors())
            return FORM;

        HtmxResponse.exito(res, "#tablaContainer", "Talla actualizada correctamente");
        cargarListado(model, q, page, sort, dir);
        return TABLA;
    }

    @PostMapping("/{id}/estatus")
    @PreAuthorize("hasAuthority('TALLAS_ESTATUS')")
    public String estatus(@PathVariable Integer id,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            Model model, HttpServletResponse res) {
        obtenerOCuatrocientosCuatro(id);
        Talla t = service.cambiarEstatus(id);
        HtmxResponse.toast(res, "Talla " + (t.isActivo() ? "activada" : "desactivada") + " correctamente");
        cargarListado(model, q, page, sort, dir);
        return TABLA;
    }

    // ---------------- helpers ----------------
    private Talla obtenerOCuatrocientosCuatro(Integer id) {
        return service.obtener(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private void cargarListado(Model model, String q, int page, String sort, String dir) {
        Page<Talla> pagina = service.buscar(q, page, sort, dir);
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
        model.addAttribute("baseUrl", "/diseno/tallas");
        model.addAttribute("qsQ", qsQ);
        model.addAttribute("qsSort", qsSort);
    }

    // Buscar tallas activas para el combo
    @GetMapping("/opciones")
    @PreAuthorize("hasAuthority('TALLAS_VER')")
    @ResponseBody
    public List<OpcionDto> opciones(@RequestParam(defaultValue = "") String q) {
        return service.buscarActivasParaCombo(q);
    }
}