package com.gamezdev.sicp.diseno.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.gamezdev.sicp.diseno.entity.Modelo;
import com.gamezdev.sicp.diseno.entity.ModeloBom;
import com.gamezdev.sicp.diseno.service.BomService;
import com.gamezdev.sicp.diseno.service.ModeloService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/diseno/fichas-tecnicas")
@RequiredArgsConstructor
public class FichaTecnicaController {

    private final ModeloService modeloService;
    private final BomService bomService;

    // Listado principal
    @GetMapping
    @PreAuthorize("hasAuthority('FICHAS_TECNICAS_VER')")
    public String listar(Model model) {
        var paginaModelos = modeloService.buscar("", 0, "codigoModelo", "asc");
        model.addAttribute("pagina", paginaModelos);
        return "diseno/fichas-tecnicas";
    }

    // Modal Individual
    @GetMapping("/{modeloId}")
    @PreAuthorize("hasAuthority('FICHAS_TECNICAS_VER')")
    public String verFicha(@PathVariable Integer modeloId, Model model) {
        Modelo modelo = modeloService.obtener(modeloId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        List<ModeloBom> versiones = bomService.obtenerVersiones(modeloId);
        ModeloBom bomActivo = versiones.isEmpty() ? null : versiones.get(0);

        model.addAttribute("modelo", modelo);
        model.addAttribute("bom", bomActivo);
        return "diseno/ficha-impresion :: individual";
    }

    // Modal Masivo
    @GetMapping("/reporte-masivo")
    @PreAuthorize("hasAuthority('FICHAS_TECNICAS_VER')")
    public String reporteMasivo(Model model) {
        var paginaModelos = modeloService.buscar("", 0, "codigoModelo", "asc");
        List<Modelo> modelosActivos = paginaModelos.getContent().stream()
                .filter(Modelo::isActivo)
                .collect(Collectors.toList());

        for (Modelo m : modelosActivos) {
            List<ModeloBom> v = bomService.obtenerVersiones(m.getId());
            m.setBomActivo(v.isEmpty() ? null : v.get(0));
        }

        model.addAttribute("modelos", modelosActivos);
        return "diseno/ficha-impresion :: masivo";
    }
}