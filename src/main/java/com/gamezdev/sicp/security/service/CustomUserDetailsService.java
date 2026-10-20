package com.gamezdev.sicp.security.service;

import com.gamezdev.sicp.security.entity.Rol;
import com.gamezdev.sicp.security.entity.Usuario;
import com.gamezdev.sicp.security.model.UsuarioDetails;
import com.gamezdev.sicp.security.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario u = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        Set<GrantedAuthority> authorities = new LinkedHashSet<>();
        List<String> nombresRoles = new ArrayList<>();

        for (Rol rol : u.getRoles()) {
            if (rol.getEstatus() != 1)
                continue; // roles dados de baja no otorgan nada
            nombresRoles.add(rol.getNombre());
            authorities.add(new SimpleGrantedAuthority("ROLE_" + rol.getCodigo()));
            rol.getPermisos().forEach(p -> authorities.add(new SimpleGrantedAuthority(p.getCodigo())));
        }

        return new UsuarioDetails(u.getId(), u.getUsername(), u.getPasswordHash(), u.getNombre(),
                u.getApellidos(), u.getEmail(), u.getEstatus() == 1, nombresRoles, authorities);
    }
}
