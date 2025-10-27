package com.example.fishingapp.security.config;

import com.example.fishingapp.security.filter.JwtAuthenticationFilter;
import com.example.fishingapp.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Deshabilitar CSRF (no es necesario con JWT)
                .csrf(AbstractHttpConfigurer::disable)

                // Configuración de CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                // Configurar autorización de peticiones
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos (sin autenticación)
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/public/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // Permitir GET en fish-captures (para mostrar capturas públicamente)
                        .requestMatchers(HttpMethod.GET, "/api/fish-captures", "/api/fish-captures/**").permitAll()

                        // Permitir GET en imágenes (para ver imágenes públicamente)
                        .requestMatchers(HttpMethod.GET,
                                "/api/captures/*/images",           // Ver imágenes de una captura
                                "/api/captures/images/*",           // Ver una imagen específica
                                "/api/captures/*/images/count"      // Contar imágenes
                        ).permitAll()

                        // Permitir GET en users
                        .requestMatchers(HttpMethod.GET, "/api/users", "/api/users/**").permitAll()

                        // Endpoints de administrador
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Endpoints de moderador
                        .requestMatchers("/api/moderator/**").hasAnyRole("MODERATOR", "ADMIN")

                        // Todos los demás endpoints requieren autenticación
                        .anyRequest().authenticated()
                )

                // Gestión de sesiones (Stateless para JWT)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Proveedor de autenticación
                .authenticationProvider(authenticationProvider())

                // Añadir filtro JWT antes del filtro de autenticación de usuario/contraseña
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}