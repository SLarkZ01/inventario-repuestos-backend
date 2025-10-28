package com.repobackend.api.auth.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.repobackend.api.auth.security.CustomUserDetailsService;
import com.repobackend.api.auth.security.JwtAuthFilter;
import com.repobackend.api.auth.security.JwtUtil;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults());
        http.csrf(csrf -> csrf.disable());
        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    http.authorizeHttpRequests(auth -> auth
        // Public auth endpoints
        .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login", "/api/auth/refresh", "/api/auth/logout").permitAll()
        .requestMatchers(HttpMethod.POST, "/api/auth/oauth/google", "/api/auth/oauth/facebook").permitAll()
        // Allow bootstrap creation of admin users (service will enforce adminKey if needed)
        .requestMatchers(HttpMethod.POST, "/api/admin/users").permitAll()
        // Public product/catalog endpoints: permitir vista sin autenticación
        .requestMatchers(HttpMethod.GET, "/api/productos", "/api/productos/**").permitAll()
        // Permitir ver stock públicamente para que clientes no autenticados puedan ver disponibilidad/precio
        .requestMatchers(HttpMethod.GET, "/api/stock").permitAll()
        // Secure these auth endpoints
        .requestMatchers("/api/auth/me", "/api/auth/revoke-all").authenticated()
        // Admin-only endpoints (other admin routes)
        .requestMatchers("/api/admin/**").hasRole("ADMIN")
        // Stock modifications: POST/PUT/DELETE requieren ADMIN o VENDEDOR
        .requestMatchers(HttpMethod.POST, "/api/stock/**").hasAnyRole("ADMIN","VENDEDOR")
        .requestMatchers(HttpMethod.PUT, "/api/stock/**").hasAnyRole("ADMIN","VENDEDOR")
        .requestMatchers(HttpMethod.DELETE, "/api/stock/**").hasAnyRole("ADMIN","VENDEDOR")
        // everything else requires authentication
        .anyRequest().authenticated()
    );

    // Disable default form login that can cause redirects to a login page
    http.formLogin(form -> form.disable());

    // Ensure unauthenticated requests result in 401 instead of redirects
    http.exceptionHandling(eh -> eh.authenticationEntryPoint((request, response, ex) ->
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED)));

        JwtAuthFilter jwtFilter = new JwtAuthFilter(jwtUtil, customUserDetailsService);
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.allowed-origins:}") String allowedOriginsProp) {
        CorsConfiguration config = new CorsConfiguration();
        if (allowedOriginsProp == null || allowedOriginsProp.isBlank()) {
            // por defecto permitir localhost:3000 para desarrollo
            config.setAllowedOrigins(List.of("http://localhost:3000"));
        } else {
            List<String> origins = Arrays.stream(allowedOriginsProp.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
            config.setAllowedOrigins(origins);
        }
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type"));
        config.setExposedHeaders(Arrays.asList("Authorization"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
