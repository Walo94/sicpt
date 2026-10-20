// ============================================
// APP GLOBAL: CSRF para HTMX, toasts, modales, errores
// ============================================

// --- CSRF en TODAS las peticiones HTMX (hx-get/post y htmx.ajax) ---
document.addEventListener("htmx:configRequest", (e) => {
  const token = document.querySelector('meta[name="_csrf"]')?.content;
  const header = document.querySelector('meta[name="_csrf_header"]')?.content;
  if (token && header) e.detail.headers[header] = token;
});

// --- Toast ---
function mostrarToast(mensaje, tipo = "success") {
  let container = document.querySelector(".toast-container");
  if (!container) {
    container = document.createElement("div");
    container.className = "toast-container";
    document.body.appendChild(container);
  }
  const icono =
    tipo === "success" ? "fa-check-circle" : "fa-exclamation-circle";
  const toast = document.createElement("div");
  toast.className = `toast show align-items-center text-white bg-${tipo} border-0 mb-2`;
  toast.innerHTML = `
    <div class="d-flex">
      <div class="toast-body"><i class="fas ${icono} me-2"></i><span></span></div>
      <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
    </div>`;
  toast.querySelector("span").textContent = mensaje; // textContent: sin XSS
  container.appendChild(toast);
  setTimeout(() => toast.remove(), 3500);
}

// Eventos enviados por el servidor con la cabecera HX-Trigger
document.addEventListener("toast", (e) =>
  mostrarToast(e.detail.mensaje, e.detail.tipo),
);

document.addEventListener("cerrarModal", () => {
  const el = document.querySelector(".modal.show");
  if (el) bootstrap.Modal.getOrCreateInstance(el).hide();
});

// --- Modal genérico: se abre cuando HTMX carga su contenido ---
document.addEventListener("htmx:afterSwap", (e) => {
  if (e.detail.target?.id === "modalContent") {
    bootstrap.Modal.getOrCreateInstance(
      document.getElementById("modalCatalogo"),
    ).show();
  }
});
document.addEventListener("shown.bs.modal", (e) => {
  e.target.querySelector("input:not([type=hidden])")?.focus();
});

// --- Errores HTTP de HTMX (4xx/5xx no se intercambian por defecto) ---
document.addEventListener("htmx:responseError", (e) => {
  const st = e.detail.xhr.status;
  if (st === 403)
    mostrarToast("No tienes permiso para realizar esta acción.", "danger");
  else if (st === 404) mostrarToast("El registro ya no existe.", "danger");
  else mostrarToast("Ocurrió un error en el servidor.", "danger");
});
document.addEventListener("htmx:sendError", () =>
  mostrarToast("No se pudo conectar con el servidor.", "danger"),
);
