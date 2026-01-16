package com.apocalipsebr.zomboid.server.manager.infrastructure.config;

import com.apocalipsebr.zomboid.server.manager.application.service.UserService;
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

    private final UserService userService;

    public SecurityConfig(UserService userService) {
        this.userService = userService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login/**", "/auth/**", "/error").permitAll()
                .requestMatchers("/items/wiki", "/items/store", "/items").permitAll() // Public item views
                .requestMatchers("/items/manage", "/items/*/edit", "/items/*/toggle-sellable").hasRole("ADMIN") // Admin item management
                .requestMatchers("/admin/**").hasRole("ADMIN") // Admin panel access
                .requestMatchers("/api/system/**").permitAll() // For deployment endpoint
                .requestMatchers("/api/zomboid-items/**").authenticated() // API requires authentication
                .requestMatchers("/player/**").authenticated() // Require authentication for player pages
                .requestMatchers("/api/**").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            )
            .securityContext(securityContext -> securityContext
                .requireExplicitSave(false) // Automatically save SecurityContext to session
            )
            .csrf(csrf -> csrf
                .disable() // Disable CSRF for APIs
            );

        return http.build();
    }
}
