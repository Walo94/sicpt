// ============================================
// LÓGICA DINÁMICA PARA EL BOM (TABLA DE CONSUMOS)
// ============================================
let filaIndex = 0;

window.inicializarBomForm = function (consumosExistentes) {
    const tbody = document.getElementById("consumosBody");
    const btnAdd = document.getElementById("btnAddMaterial");
    
    if (!tbody) return;
    
    // Limpiamos para evitar duplicados si HTMX recarga el modal por un error de validación
    tbody.innerHTML = ""; 
    
    // Nos aseguramos de que el índice continúe desde donde se quedó si hay consumos existentes
    filaIndex = 0;

    function crearFila(materialId = "", materialTexto = "", pieza = "", cantidad = "") {

        const cantidadFormateada = cantidad ? parseFloat(cantidad).toFixed(4) : "";
        
        const tr = document.createElement("tr");
         tr.innerHTML = `
            <td style="padding: 8px 10px;">
                <input type="text" class="form-control form-control-sm text-uppercase" 
                       name="consumos[${filaIndex}].piezaCalzado" value="${pieza}" 
                       placeholder="Ej: Chinela" required>
            </td>
            <td style="padding: 8px 10px;">
                <div class="combo-busqueda" data-url="/mp/materiales/opciones">
                    <input type="text" class="form-control form-control-sm" 
                           value="${materialTexto}" placeholder="Buscar material..." autocomplete="off">
                    <input type="hidden" name="consumos[${filaIndex}].materialId" value="${materialId}">
                    <div class="combo-busqueda-dropdown"></div>
                </div>
            </td>
            <td style="padding: 8px 10px;">
                <input type="number" class="form-control form-control-sm" 
                       name="consumos[${filaIndex}].cantidadConsumo" value="${cantidadFormateada}" 
                       step="0.0001" min="0.0001" placeholder="0.0000" required>
            </td>
            <td class="text-center" style="padding: 8px 10px;">
                <button type="button" class="btn btn-danger btn-sm btn-remove-row">
                    <i class="fas fa-times"></i>
                </button>
            </td>
        `;
        
        tr.querySelector(".btn-remove-row").addEventListener("click", () => tr.remove());
        tbody.appendChild(tr);
        
        // Inicializar el combo para la nueva fila
        if (window.initCombos) window.initCombos();
        
        filaIndex++;
    }

    // 1. Cargar filas iniciales
    if (consumosExistentes && consumosExistentes.length > 0) {
        consumosExistentes.forEach(c => {
            crearFila(c.materialId, c.materialTexto, c.piezaCalzado, c.cantidadConsumo);
        });
    } else {
        crearFila(); // Fila vacía por defecto
    }

    // 2. Evento del botón Agregar
    if (btnAdd && !btnAdd.dataset.init) {
        btnAdd.dataset.init = "1"; // Evitar múltiples listeners
        btnAdd.addEventListener("click", () => crearFila());
    }
};

// Escuchar cuando HTMX inyecta el modal en la página
document.addEventListener("htmx:afterSwap", (e) => {
    if (e.detail.target?.id === "modalContent") {
        const jsonInput = document.getElementById("consumosJson");
        let consumos = [];

        if (jsonInput && jsonInput.value) {
            try {
                consumos = JSON.parse(jsonInput.value);
            } catch (err) {
                console.error("Error al parsear consumos:", err);
            }
        }

        if (window.inicializarBomForm) {
            window.inicializarBomForm(consumos);
        }

        // En edición, el foco va a Descripción
        const form = document.getElementById("formBom");
        if (form && form.dataset.editando === "true") {
            const enfocar = () => {
                const desc = document.getElementById("descripcion");
                if (desc) {
                    desc.focus();
                    desc.setSelectionRange(desc.value.length, desc.value.length); // cursor al final
                }
            };
            const modal = document.getElementById("modalCatalogo");
            if (modal && modal.classList.contains("show")) {
                // Modal ya visible (p. ej. re-render por error de validación)
                setTimeout(enfocar, 50);
            } else if (modal) {
                // Modal aún animándose: esperamos a que termine
                modal.addEventListener("shown.bs.modal", enfocar, { once: true });
            }
        }
    }
});