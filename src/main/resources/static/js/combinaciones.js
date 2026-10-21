// ============================================
// COMBINACIONES: Manejo de Copia Temporal
// ============================================
(function () {
  const CLAVE_STORAGE = "sicp_combinacion_copia";

  // 1. Interceptar el clic en "Copiar combinación"
  document.addEventListener("click", (e) => {
    const btnCopiar = e.target.closest('[data-action="copiar"]');
    if (btnCopiar) {
      e.preventDefault();
      
      const fila = document.querySelector(".catalogos-tabla tbody tr.selected");
      if (!fila) return;
      
      const id = fila.getAttribute("data-id");

      // Hacemos un fetch invisible para obtener el HTML del form de edición
      fetch(`/diseno/combinaciones/${id}/editar`, { headers: { "HX-Request": "true" } })
        .then((r) => r.text())
        .then((html) => {
          // Parseamos el HTML a un documento temporal
          const parser = new DOMParser();
          const doc = parser.parseFromString(html, "text/html");
          
          // Extraemos los valores
          const copia = {
            modeloId: doc.getElementById("modeloId")?.value,
            modeloTexto: doc.getElementById("modeloBuscar")?.value,
            colorId: doc.getElementById("colorId")?.value,
            colorTexto: doc.getElementById("colorBuscar")?.value,
            tallaId: doc.getElementById("tallaId")?.value,
            tallaTexto: doc.getElementById("tallaBuscar")?.value,
            unidadVentaId: doc.getElementById("unidadVentaId")?.value,
            unidadVentaTexto: doc.getElementById("unidadBuscar")?.value,
            claveSatProducto: doc.querySelector('[name="claveSatProducto"]')?.value,
            precioVentaBase: doc.querySelector('[name="precioVentaBase"]')?.value
          };

          // Guardamos en sessionStorage
          sessionStorage.setItem(CLAVE_STORAGE, JSON.stringify(copia));
          
          // Notificamos al usuario
          if (window.mostrarToast) mostrarToast("Combinación copiada. Ve a 'Agregar Combinación' para pegarla.", "info");
        })
        .catch((err) => console.error("Error al copiar", err));
    }
  });

  // 2. Rellenar el formulario si es "Nuevo" y hay una copia
  document.addEventListener("htmx:afterSwap", (e) => {
    if (e.detail.target?.id === "modalContent") {
      const form = document.getElementById("formCombinacion");
      if (!form) return;

      // Verificamos si el form es de "Nuevo" por el título (o por la ausencia del campo ID)
      const idInput = form.querySelector('[name="id"]');
      const esNuevo = !idInput || !idInput.value || idInput.value === "";

      if (!esNuevo) return; // Si es edición, no hacemos nada

      const dataStr = sessionStorage.getItem(CLAVE_STORAGE);
      if (!dataStr) return;

      try {
        const data = JSON.parse(dataStr);
        
        // Rellenar inputs ocultos
        form.querySelector('[name="modeloId"]').value = data.modeloId || "";
        form.querySelector('[name="colorId"]').value = data.colorId || "";
        form.querySelector('[name="tallaId"]').value = data.tallaId || "";
        form.querySelector('[name="unidadVentaId"]').value = data.unidadVentaId || "";
        
        // Rellenar campos normales
        form.querySelector('[name="claveSatProducto"]').value = data.claveSatProducto || "53111500";
        form.querySelector('[name="precioVentaBase"]').value = data.precioVentaBase || "0.00";

        // Rellenar inputs visibles (combos)
        document.getElementById("modeloBuscar").value = data.modeloTexto || "";
        document.getElementById("colorBuscar").value = data.colorTexto || "";
        document.getElementById("tallaBuscar").value = data.tallaTexto || "";
        document.getElementById("unidadBuscar").value = data.unidadVentaTexto || "";

        // Asegurarnos de que el id principal esté vacío (por si acaso)
        if (idInput) idInput.value = "";

        // Enfocar el código de combinación para que lo cambie de inmediato
        const inputCodigo = form.querySelector('[name="codigoCombinacion"]');
        if (inputCodigo) {
          inputCodigo.value = ""; // Lo limpiamos para que el usuario asigne uno nuevo
          inputCodigo.focus();
        }
      } catch (err) {
        console.error("Error al leer copia", err);
      }
    }
  });

  // 3. Limpiar copia (al guardar exitoso, cancelar o cambiar pestaña)
  document.addEventListener("htmx:afterRequest", (e) => {
    // Si el post a combinaciones tuvo éxito
    if (e.detail.elt?.id === "formCombinacion" && e.detail.successful) {
      sessionStorage.removeItem(CLAVE_STORAGE);
    }
  });

  document.addEventListener("click", (e) => {
    // Si da clic en el botón cancelar
    if (e.target.closest('#modalContent [data-bs-dismiss="modal"]')) {
      sessionStorage.removeItem(CLAVE_STORAGE);
    }
    // Si cambia de pestaña en la cinta
    if (e.target.closest('.ribbon-tabs .nav-link')) {
      sessionStorage.removeItem(CLAVE_STORAGE);
    }
  });
})();