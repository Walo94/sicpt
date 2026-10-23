// Muestra/oculta el combo de talla según el switch "¿Maneja talla?"
window.toggleTalla = function (chk) {
  const bloque = document.getElementById("tallaBloque");
  if (!bloque) return;

  bloque.classList.toggle("d-none", !chk.checked);

  const buscar = document.getElementById("tallaBuscar");
  const hidden = document.getElementById("tallaId");

  if (chk.checked) {
    if (buscar) buscar.focus();
  } else {
    // Si se desmarca, se limpia la talla elegida
    if (buscar) buscar.value = "";
    if (hidden) hidden.value = "";
  }
};