package com.gamezdev.sicp.mp.controller;

import com.gamezdev.sicp.common.util.HtmxResponse;
import com.gamezdev.sicp.diseno.service.TallaService;
import com.gamezdev.sicp.diseno.service.UnidadService;
import com.gamezdev.sicp.mp.dto.MaterialForm;
import com.gamezdev.sicp.mp.entity.Material;
import com.gamezdev.sicp.mp.service.MaterialService;
import com.gamezdev.sicp.mp.service.MaterialService.DuplicadoException;
import com.gamezdev.sicp.mp.service.MaterialService.UnidadInvalidaException;
import com.gamezdev.sicp.mp.service.MaterialService.TallaInvalidaException;
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
@RequestMapping("/mp/materiales")
@RequiredArgsConstructor
public class MaterialController {

    private static final String VISTA = "mp/materiales";
    private static final String TABLA = "mp/materiales-tabla :: tabla";
    private static final String FORM = "mp/material-form :: formulario";

    private final MaterialService service;
    private final UnidadService unidadService;
    private final TallaService tallaService;

    @GetMapping
    @PreAuthorize("hasAuthority('MATERIALES_VER')")
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
    @PreAuthorize("hasAuthority('MATERIALES_CREAR')")
    public String nuevo(Model model) {
        model.addAttribute("form", new MaterialForm());
        return FORM;
    }

    @GetMapping("/{id}/editar")
    @PreAuthorize("hasAuthority('MATERIALES_EDITAR')")
    public String editar(@PathVariable Integer id, Model model) {
        Material m = obtenerOCuatrocientosCuatro(id);
        MaterialForm form = new MaterialForm();
        form.setId(m.getId());
        form.setCodigo(m.getCodigo());
        form.setDescripcion(m.getDescripcion());
        form.setUnidadCompraId(m.getUnidadCompra().getId());
        form.setUnidadCompraTexto(label(m.getUnidadCompra().getCodigo(), m.getUnidadCompra().getDescripcion()));
        form.setUnidadConsumoId(m.getUnidadConsumo().getId());
        form.setUnidadConsumoTexto(label(m.getUnidadConsumo().getCodigo(), m.getUnidadConsumo().getDescripcion()));
        form.setFactorConversion(m.getFactorConversion());
        form.setTipoMaterial(m.getTipoMaterial());
        form.setManejaTalla(m.isConTalla());
        if (m.isConTalla() && m.getTalla() != null) {
            form.setTallaId(m.getTalla().getId());
            form.setTallaTexto(TallaService.etiqueta(m.getTalla()));
        }
        if (m.isConTalla() && m.getTalla() != null) {
            form.setTallaId(m.getTalla().getId());
            form.setTallaTexto(m.getTalla().getDescripcion());
        }
        model.addAttribute("form", form);
        return FORM;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MATERIALES_CREAR')")
    public String crear(@Valid @ModelAttribute("form") MaterialForm form, BindingResult br,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            Model model, HttpServletResponse res) {
        form.setId(null);
        if (!br.hasErrors()) {
            try {
                service.crear(form.getCodigo(), form.getDescripcion(),
                        form.getUnidadCompraId(), form.getUnidadConsumoId(),
                        form.getFactorConversion(), form.getTipoMaterial(),
                        form.isManejaTalla(), form.getTallaId());
            } catch (DuplicadoException e) {
                br.rejectValue("codigo", "duplicado", e.getMessage());
            } catch (UnidadInvalidaException e) {
                // El mensaje menciona "compra" o "consumo", lo mandamos al campo adecuado
                if (e.getMessage().contains("compra")) {
                    br.rejectValue("unidadCompraId", "unidadInvalida", e.getMessage());
                } else if (e.getMessage().contains("consumo")) {
                    br.rejectValue("unidadConsumoId", "unidadInvalida", e.getMessage());
                } else {
                    br.rejectValue("factorConversion", "factorInvalido", e.getMessage());
                }
            } catch (TallaInvalidaException e) {
                br.rejectValue("tallaId", "tallaInvalida", e.getMessage());
            }
        }
        if (br.hasErrors()) {
            refrescarLabels(form);
            return FORM;
        }

        HtmxResponse.exito(res, "#tablaContainer", "Material creado correctamente");
        cargarListado(model, q, page, sort, dir);
        return TABLA;
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAuthority('MATERIALES_EDITAR')")
    public String actualizar(@PathVariable Integer id,
            @Valid @ModelAttribute("form") MaterialForm form, BindingResult br,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            Model model, HttpServletResponse res) {
        obtenerOCuatrocientosCuatro(id);
        form.setId(id);
        if (!br.hasErrors()) {
            try {
                service.actualizar(id, form.getCodigo(), form.getDescripcion(),
                        form.getUnidadCompraId(), form.getUnidadConsumoId(),
                        form.getFactorConversion(), form.getTipoMaterial(),
                        form.isManejaTalla(), form.getTallaId());
            } catch (DuplicadoException e) {
                br.rejectValue("codigo", "duplicado", e.getMessage());
            } catch (UnidadInvalidaException e) {
                if (e.getMessage().contains("compra")) {
                    br.rejectValue("unidadCompraId", "unidadInvalida", e.getMessage());
                } else if (e.getMessage().contains("consumo")) {
                    br.rejectValue("unidadConsumoId", "unidadInvalida", e.getMessage());
                } else {
                    br.rejectValue("factorConversion", "factorInvalido", e.getMessage());
                }
            } catch (TallaInvalidaException e) {
                br.rejectValue("tallaId", "tallaInvalida", e.getMessage());
            }
        }
        if (br.hasErrors()) {
            refrescarLabels(form);
            return FORM;
        }

        HtmxResponse.exito(res, "#tablaContainer", "Material actualizado correctamente");
        cargarListado(model, q, page, sort, dir);
        return TABLA;
    }

    @PostMapping("/{id}/estatus")
    @PreAuthorize("hasAuthority('MATERIALES_ESTATUS')")
    public String estatus(@PathVariable Integer id,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            Model model, HttpServletResponse res) {
        obtenerOCuatrocientosCuatro(id);
        Material m = service.cambiarEstatus(id);
        HtmxResponse.toast(res, "Material " + (m.isActivo() ? "activado" : "desactivado") + " correctamente");
        cargarListado(model, q, page, sort, dir);
        return TABLA;
    }

    // ---------------- helpers ----------------
    private String label(String codigo, String desc) {
        return codigo + " - " + desc;
    }

    private void refrescarLabels(MaterialForm form) {
        if (form.getUnidadCompraId() != null) {
            unidadService.obtener(form.getUnidadCompraId())
                    .ifPresent(u -> form.setUnidadCompraTexto(label(u.getCodigo(), u.getDescripcion())));
        }
        if (form.getUnidadConsumoId() != null) {
            unidadService.obtener(form.getUnidadConsumoId())
                    .ifPresent(u -> form.setUnidadConsumoTexto(label(u.getCodigo(), u.getDescripcion())));
        }

        if (!form.isManejaTalla()) {
            form.setTallaId(null);
            form.setTallaTexto(null);
        } else if (form.getTallaId() != null) {
            tallaService.obtener(form.getTallaId())
                    .ifPresent(t -> form.setTallaTexto(TallaService.etiqueta(t)));
        }
    }

    private Material obtenerOCuatrocientosCuatro(Integer id) {
        return service.obtener(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private void cargarListado(Model model, String q, int page, String sort, String dir) {
        Page<Material> pagina = service.buscar(q, page, sort, dir);
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
        model.addAttribute("baseUrl", "/mp/materiales");
        model.addAttribute("qsQ", qsQ);
        model.addAttribute("qsSort", qsSort);
    }

    @GetMapping("/opciones")
    @PreAuthorize("hasAnyAuthority('MATERIALES_VER','BOM_VER','BOM_CREAR','BOM_EDITAR')")
    @ResponseBody
    public List<com.gamezdev.sicp.common.dto.OpcionDto> opciones(
            @RequestParam(defaultValue = "") String q) {
        return service.buscarActivasParaCombo(q);
    }
}