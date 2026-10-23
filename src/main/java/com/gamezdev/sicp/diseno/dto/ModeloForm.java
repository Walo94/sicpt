package com.gamezdev.sicp.diseno.dto;

import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModeloForm {

    private Integer id;

    @NotBlank(message = "El código del modelo es obligatorio.")
    @Size(max = 50, message = "Máximo 50 caracteres.")
    private String codigoModelo;

    @NotBlank(message = "La descripción es obligatoria.")
    @Size(max = 150, message = "Máximo 150 caracteres.")
    private String descripcion;

    @NotNull(message = "Debes seleccionar una marca.")
    private Integer marcaId;
    private String marcaTexto;

    @NotNull(message = "Debes seleccionar una línea.")
    private Integer lineaId;
    private String lineaTexto;

    @NotNull(message = "Debes seleccionar una horma.")
    private Integer hormaId;
    private String hormaTexto;

    // --- NUEVOS CAMPOS ---
    private String materialCorte;
    private String forro;
    private String plantilla;
    private String suela;
    private String colorPrincipal;
    private String estilo;
    private String cordones;
    private String contrafuerte;
    private String cuello;
    private String lengueta;
    private String ojales;
    private String vidaUtil;
    private String tallasDisponibles;
    private String observaciones;
    private String recomendaciones;
    private String almacenamiento;

    // Para la imagen
    private String imagenUrlActual; // Para saber si ya tenía una imagen
    private MultipartFile imagenFile; // El archivo que sube el usuario
}