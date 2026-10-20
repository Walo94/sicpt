package com.gamezdev.sicp.config;

import com.gamezdev.sicp.security.handler.LoginSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;

@Configuration
@EnableMethodSecurity // habilita @PreAuthorize en los controllers
public class SecurityConfig {

        @Bean
        PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        SecurityFilterChain securityFilterChain(
                        HttpSecurity http,
                        LoginSuccessHandler loginSuccessHandler) throws Exception {

                // Si la sesión expira durante una petición HTMX, redirige toda la página al
                // login.
                AuthenticationEntryPoint htmxEntryPoint = (request, response, ex) -> {
                        response.setHeader("HX-Redirect", request.getContextPath() + "/login");
                        response.setStatus(401);
                };

                http
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/css/**",
                                                                "/js/**",
                                                                "/images/**",
                                                                "/icons/**",
                                                                "/login",
                                                                "/error",
                                                                "/error/**")
                                                .permitAll()
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .loginProcessingUrl("/login")
                                                .usernameParameter("username")
                                                .passwordParameter("password")
                                                // Actualiza el último acceso sin registrar LOGIN en auditoría.
                                                .successHandler(loginSuccessHandler)
                                                .failureUrl("/login?error")
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutUrl("/logout") // POST + CSRF
                                                // No se agrega AuditoriaLogoutHandler: el logout no genera auditoría.
                                                .logoutSuccessUrl("/login?logout")
                                                .invalidateHttpSession(true)
                                                .deleteCookies("JSESSIONID")
                                                .permitAll())
                                .exceptionHandling(ex -> ex
                                                .defaultAuthenticationEntryPointFor(
                                                                htmxEntryPoint,
                                                                new RequestHeaderRequestMatcher("HX-Request", "true")));

                // CSRF permanece habilitado.
                return http.build();
        }

}