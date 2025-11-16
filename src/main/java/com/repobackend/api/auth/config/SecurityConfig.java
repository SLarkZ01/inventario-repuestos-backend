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
            // ====== CRITICAL: Public anonymous endpoints FIRST ======
            // Public carrito endpoints: permitir carritos anónimos - TODAS LAS OPERACIONES (sin restricción de método)
            .requestMatchers("/api/carritos", "/api/carritos/**").permitAll()
            // Public favoritos endpoints: permitir favoritos anónimos
            .requestMatchers("/api/favoritos", "/api/favoritos/**").permitAll()
            // ====== Auth endpoints ======
            // Public auth endpoints
            .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login", "/api/auth/refresh", "/api/auth/logout").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/auth/oauth/google").permitAll()
            // Allow bootstrap creation of admin users (service will enforce adminKey if needed)
            .requestMatchers(HttpMethod.POST, "/api/admin/users").permitAll()
            // Springdoc OpenAPI endpoints (swagger-ui, api-docs)
            .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/webjars/**").permitAll()
            // Public simplified categories endpoint used by mobile app
            .requestMatchers(HttpMethod.GET, "/api/public/categorias", "/api/public/categorias/**").permitAll()
            // Public product/catalog endpoints: permitir vista sin autenticación
            .requestMatchers(HttpMethod.GET, "/api/productos", "/api/productos/**").permitAll()
            // Public categories listing/details
            .requestMatchers(HttpMethod.GET, "/api/categorias", "/api/categorias/**").permitAll()
            // Protect modifications to products: ADMIN or VENDEDOR
            .requestMatchers(HttpMethod.POST, "/api/productos/**").hasAnyRole("ADMIN","VENDEDOR")
            .requestMatchers(HttpMethod.PUT, "/api/productos/**").hasAnyRole("ADMIN","VENDEDOR")
            .requestMatchers(HttpMethod.DELETE, "/api/productos/**").hasAnyRole("ADMIN","VENDEDOR")
            // Protect modifications to categories: ADMIN or VENDEDOR
            .requestMatchers(HttpMethod.POST, "/api/categorias/**").hasAnyRole("ADMIN","VENDEDOR")
            .requestMatchers(HttpMethod.PUT, "/api/categorias/**").hasAnyRole("ADMIN","VENDEDOR")
            .requestMatchers(HttpMethod.DELETE, "/api/categorias/**").hasAnyRole("ADMIN","VENDEDOR")
            // Permitir ver stock públicamente para que clientes no autenticados puedan ver disponibilidad/precio
            .requestMatchers(HttpMethod.GET, "/api/stock").permitAll()
            // Facturas: Solo usuarios autenticados pueden crear y consultar facturas
            .requestMatchers(HttpMethod.POST, "/api/facturas/checkout").authenticated()
            .requestMatchers(HttpMethod.GET, "/api/facturas", "/api/facturas/**").authenticated()
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

    @Value("${app.cors.allowed-origins:}")
    private String corsOriginsConfig;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        String allowedOrigins = corsOriginsConfig.trim();
        if (allowedOrigins.isEmpty()) {
            configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        } else {
            configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        }
        configuration.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS","PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
