package back.ecommerce.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer; // Importante
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration; // Importante
import org.springframework.web.cors.CorsConfigurationSource; // Importante
import org.springframework.web.cors.UrlBasedCorsConfigurationSource; // Importante

import back.ecommerce.config.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Configuración de CORS: Permite que el navegador confíe en tu API
            .cors(Customizer.withDefaults()) 
            
            // 2. Desactivar CSRF (Correcto para APIs Stateless)
            .csrf(AbstractHttpConfigurer::disable)
            
            // 3. Configurar Permisos de Rutas
            .authorizeHttpRequests(auth -> auth
                // Rutas Públicas
                .requestMatchers("/api/auth/**").permitAll() // Login, Register, Verify
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll() // Docs
                .requestMatchers("/api/pagos/webhook").permitAll() // Webhook MP
                .requestMatchers(HttpMethod.GET, "/api/tiendas/**").permitAll() // Ver tiendas y productos es público

                // 🚨 OJO: El endpoint de Storage para subir imágenes debe estar autenticado
                // Si quieres que sea público (riesgoso), agrégalo aquí. 
                // Por defecto caerá en el .authenticated() de abajo, lo cual es CORRECTO.

                // Todo lo demás requiere autenticación
                .anyRequest().authenticated()
            )

            // 4. Configurar Sesión (Stateless)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // 5. Provider y Filtro JWT
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ✅ Bean de Configuración Global de CORS
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Configura aquí la URL de tu frontend en producción para mayor seguridad
        // Ej: configuration.setAllowedOrigins(List.of("https://mi-frontend.render.com", "http://localhost:5173"));
        configuration.setAllowedOrigins(List.of("*")); // Permite todo (útil para dev)
        
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}