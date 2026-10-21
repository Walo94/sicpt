package com.gamezdev.sicp.diseno.controller;

import com.gamezdev.sicp.diseno.service.HormaService;
import com.gamezdev.sicp.diseno.service.LineaService;
import com.gamezdev.sicp.diseno.service.MarcaService;
import com.gamezdev.sicp.common.dto.OpcionDto;
import com.gamezdev.sicp.common.util.HtmxResponse;
import com.gamezdev.sicp.diseno.dto.ModeloForm;
import com.gamezdev.sicp.diseno.entity.Modelo;
import com.gamezdev.sicp.diseno.service.ModeloService;
import com.gamezdev.sicp.diseno.service.ModeloService.DuplicadoException;
import com.gamezdev.sicp.diseno.service.ModeloService.ReferenciaInvalidaException;
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
@RequestMapping("/diseno/modelos")
@RequiredArgsConstructor
public class ModeloController {

    private static final String VISTA = "diseno/modelos";
    private static final String TABLA = "diseno/modelos-tabla :: tabla";
    private static final String FORM = "diseno/modelo-form :: formulario";

    private final ModeloService service;
    private final MarcaService marcaService;
    private final LineaService lineaService;
    private final HormaService hormaService;

    @GetMapping
    @PreAuthorize("hasAuthority('MODELOS_VER')")
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
    @PreAuthorize("hasAuthority('MODELOS_CREAR')")
    public String nuevo(Model model) {
        model.addAttribute("form", new ModeloForm());
        return FORM;
    }

    @GetMapping("/{id}/editar")
    @PreAuthorize("hasAuthority('MODELOS_EDITAR')")
    public String editar(@PathVariable Integer id, Model model) {
        Modelo m = obtenerOCuatrocientosCuatro(id);
        ModeloForm form = new ModeloForm();
        form.setId(m.getId());
        form.setCodigoModelo(m.getCodigoModelo());
        form.setDescripcion(m.getDescripcion());
        form.setMarcaId(m.getMarca().getId());
        form.setMarcaTexto(m.getMarca().getDescripcion());
        form.setLineaId(m.getLinea().getId());
        form.setLineaTexto(m.getLinea().getDescripcion());
        form.setHormaId(m.getHorma().getId());
        form.setHormaTexto(m.getHorma().getClaveHorma() + " - " + m.getHorma().getDescripcion());
        model.addAttribute("form", form);
        return FORM;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MODELOS_CREAR')")
    public String crear(@Valid @ModelAttribute("form") ModeloForm form, BindingResult br,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            Model model, HttpServletResponse res) {
        form.setId(null);
        if (!br.hasErrors()) {
            try {
                service.crear(form.getCodigoModelo(), form.getDescripcion(),
                        form.getMarcaId(), form.getLineaId(), form.getHormaId());
            } catch (DuplicadoException e) {
                br.rejectValue("codigoModelo", "duplicado", e.getMessage());
            } catch (ReferenciaInvalidaException e) {
                mapearReferencia(br, e.getMessage());
            }
        }
        if (br.hasErrors()) {
            refrescarLabels(form);
            return FORM;
        }

        HtmxResponse.exito(res, "#tablaContainer", "Modelo creado correctamente");
        cargarListado(model, q, page, sort, dir);
        return TABLA;
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAuthority('MODELOS_EDITAR')")
    public String actualizar(@PathVariable Integer id,
            @Valid @ModelAttribute("form") ModeloForm form, BindingResult br,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            Model model, HttpServletResponse res) {
        obtenerOCuatrocientosCuatro(id);
        form.setId(id);
        if (!br.hasErrors()) {
            try {
                service.actualizar(id, form.getCodigoModelo(), form.getDescripcion(),
                        form.getMarcaId(), form.getLineaId(), form.getHormaId());
            } catch (DuplicadoException e) {
                br.rejectValue("codigoModelo", "duplicado", e.getMessage());
            } catch (ReferenciaInvalidaException e) {
                mapearReferencia(br, e.getMessage());
            }
        }
        if (br.hasErrors()) {
            refrescarLabels(form);
            return FORM;
        }

        HtmxResponse.exito(res, "#tablaContainer", "Modelo actualizado correctamente");
        cargarListado(model, q, page, sort, dir);
        return TABLA;
    }

    @PostMapping("/{id}/estatus")
    @PreAuthorize("hasAuthority('MODELOS_ESTATUS')")
    public String estatus(@PathVariable Integer id,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            Model model, HttpServletResponse res) {
        obtenerOCuatrocientosCuatro(id);
        Modelo m = service.cambiarEstatus(id);
        HtmxResponse.toast(res, "Modelo " + (m.isActivo() ? "activado" : "desactivado") + " correctamente");
        cargarListado(model, q, page, sort, dir);
        return TABLA;
    }

    // ---------------- helpers ----------------
    private void mapearReferencia(BindingResult br, String msg) {
        if (msg.contains("marca")) {
            br.rejectValue("marcaId", "refInvalida", msg);
        } else if (msg.contains("línea")) {
            br.rejectValue("lineaId", "refInvalida", msg);
        } else if (msg.contains("horma")) {
            br.rejectValue("hormaId", "refInvalida", msg);
        }
    }

    private void refrescarLabels(ModeloForm form) {
        if (form.getMarcaId() != null) {
            marcaService.obtener(form.getMarcaId()).ifPresent(x -> form.setMarcaTexto(x.getDescripcion()));
        }
        if (form.getLineaId() != null) {
            lineaService.obtener(form.getLineaId()).ifPresent(x -> form.setLineaTexto(x.getDescripcion()));
        }
        if (form.getHormaId() != null) {
            hormaService.obtener(form.getHormaId())
                    .ifPresent(x -> form.setHormaTexto(x.getClaveHorma() + " - " + x.getDescripcion()));
        }
    }

    private Modelo obtenerOCuatrocientosCuatro(Integer id) {
        return service.obtener(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private void cargarListado(Model model, String q, int page, String sort, String dir) {
        Page<Modelo> pagina = service.buscar(q, page, sort, dir);
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
        model.addAttribute("baseUrl", "/diseno/modelos");
        model.addAttribute("qsQ", qsQ);
        model.addAttribute("qsSort", qsSort);
    }

    @GetMapping("/opciones")
    @PreAuthorize("hasAnyAuthority('COMBINACIONES_VER','COMBINACIONES_CREAR','COMBINACIONES_EDITAR','MODELOS_VER')")
    @ResponseBody
    public List<OpcionDto> opciones(@RequestParam(defaultValue = "") String q) {
        return service.buscarActivasParaCombo(q);
    }
}