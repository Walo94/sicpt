package com.gamezdev.sicp.diseno.dto;

import java.util.ArrayList;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BomForm {
    private Integer id;

    @NotNull(message = "Debes seleccionar un modelo.")
    private Integer modeloId;
    private String modeloTexto;

    @NotNull(message = "La versión es obligatoria.")
    private Integer version = 1;

    private String descripcion;

    @Valid
    @NotEmpty(message = "Debes agregar al menos un material al BOM.")
    private List<ConsumoItemForm> consumos = new ArrayList<>();
}