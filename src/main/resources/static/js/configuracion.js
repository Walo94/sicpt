// ============================================
// CONFIGURACIÓN: ayudas del formulario de usuarios y roles
// (delegación en document: el modal se carga por HTMX)
// ============================================
document.addEventListener("click", (e) => {
  // Mostrar / ocultar contraseña
  const btnPass = e.target.closest("[data-toggle-password]");
  if (btnPass) {
    const input = btnPass.parentElement.querySelector("input");
    const oculto = input.type === "password";
    input.type = oculto ? "text" : "password";
    btnPass.querySelector("i").className = oculto
      ? "fas fa-eye-slash"
      : "fas fa-eye";
    return;
  }

  // Matriz de permisos: por módulo
  const todos = e.target.closest("[data-permisos-todos]");
  const ninguno = e.target.closest("[data-permisos-ninguno]");
  if (todos || ninguno) {
    e.preventDefault();
    e.target
      .closest(".permisos-modulo")
      .querySelectorAll('input[name="permisoIds"]:not(:disabled)')
      .forEach((c) => (c.checked = !!todos));
    return;
  }

  // Matriz de permisos: global
  const gTodos = e.target.closest("[data-permisos-todos-global]");
  const gNinguno = e.target.closest("[data-permisos-ninguno-global]");
  if (gTodos || gNinguno) {
    e.preventDefault();
    document
      .querySelectorAll(
        '.permisos-matriz input[name="permisoIds"]:not(:disabled)',
      )
      .forEach((c) => (c.checked = !!gTodos));
  }
});
