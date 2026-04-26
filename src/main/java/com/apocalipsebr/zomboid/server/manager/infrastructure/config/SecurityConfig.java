package com.apocalipsebr.zomboid.server.manager.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index", "/season-guide", "/map-regions/**", "/pzmap/**", "/login/**",
                                "/auth/**", "/error", "/map.jpg", "/map-tiles/**", "/login.png", "/UmbrellaCorp.png")
                        .permitAll()
                        .requestMatchers("/api/system/**").permitAll() // For deployment endpoint
                        .requestMatchers("/api/zombie-kills/**").permitAll() // For game server updates
                        .requestMatchers("/donations/webhook").permitAll() // PagBank webhook callback
                        .requestMatchers("/items/manage", "/items/*/edit", "/items/*/toggle-sellable").hasRole("ADMIN") // Admin
                                                                                                                        // item
                                                                                                                        // management
                        .requestMatchers("/admin/**").hasRole("ADMIN") // Admin panel access
                        .requestMatchers("/nfe/**").hasRole("ADMIN") // NF-e management
                        .anyRequest().authenticated() // All other routes require authentication
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID"))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false))
                .securityContext(securityContext -> securityContext
                        .requireExplicitSave(false) // Automatically save SecurityContext to session
                )
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin()) // Allow iframe embedding from same origin
                                                                   // (map-regions modal)
                )
                .csrf(csrf -> csrf
                        .disable() // Disable CSRF for APIs
                );

        return http.build();
    }
}
