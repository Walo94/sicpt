document.addEventListener("DOMContentLoaded", () => {
  const pass = document.getElementById("password");
  const btnVer = document.getElementById("btnVerPassword");
  const form = document.getElementById("formLogin");
  const btnLogin = document.getElementById("btnLogin");

  btnVer.addEventListener("click", () => {
    const oculto = pass.type === "password";
    pass.type = oculto ? "text" : "password";
    btnVer.querySelector("i").className = oculto
      ? "fas fa-eye-slash"
      : "fas fa-eye";
  });

  form.addEventListener("submit", (e) => {
    if (!form.username.value.trim() || !pass.value) {
      e.preventDefault();
      (form.username.value.trim() ? pass : form.username).focus();
      return;
    }
    btnLogin.disabled = true;
    btnLogin.innerHTML =
      '<span class="spinner-border spinner-border-sm me-1"></span> Entrando...';
  });
});
