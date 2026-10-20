package com.gamezdev.sicp.security.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

public class UsuarioDetails implements UserDetails {

    private final Integer id;
    private final String username;
    private final String password;
    private final String nombre;
    private final String apellidos;
    private final String email;
    private final boolean activo;
    private final List<String> roles;
    private final Collection<? extends GrantedAuthority> authorities;

    public UsuarioDetails(Integer id, String username, String password, String nombre, String apellidos,
            String email, boolean activo, List<String> roles,
            Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.activo = activo;
        this.roles = roles;
        this.authorities = authorities;
    }

    public Integer getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public List<String> getRoles() {
        return roles;
    }

    public String getNombreCompleto() {
        return (nombre + " " + (apellidos == null ? "" : apellidos)).trim();
    }

    public String getRolesTexto() {
        return String.join(", ", roles);
    }

    public String getIniciales() {
        String a = nombre.isEmpty() ? "" : nombre.substring(0, 1);
        String b = (apellidos == null || apellidos.isEmpty()) ? "" : apellidos.substring(0, 1);
        return (a + b).toUpperCase();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return activo;
    }

}
