package back.ecommerce.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

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
            // 1. ✅ CORS ACTIVADO: Fundamental para que el front se comunique con el back
            .cors(Customizer.withDefaults())
            
            // 2. CSRF Desactivado (Correcto para JWT/Stateless)
            .csrf(AbstractHttpConfigurer::disable)
            
            // 3. Reglas de Autorización (EL ORDEN IMPORTA)
            .authorizeHttpRequests(auth -> auth
                // A. Rutas PÚBLICAS (Login, Registro, Docs, Ver Tiendas)
                // Usamos requestMatchers con HttpMethod para ser más específicos
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**").permitAll()
                .requestMatchers("/api/pagos/webhook").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/tiendas/**").permitAll() // Ver productos es público
                
                // B. Rutas PROTEGIDAS (Subir imágenes, Crear tiendas, Comprar)
                // Cloudinary: Solo usuarios logueados pueden subir fotos
                .requestMatchers("/api/storage/**").authenticated() 
                
                // C. Todo lo demás requiere login
                .anyRequest().authenticated()
            )

            // 4. Gestión de Sesión (Stateless)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // 5. Providers y Filtros
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ✅ CONFIGURACIÓN DE CORS (Lo que te está fallando)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // En desarrollo "*" está bien. En producción, pon la URL de tu Render/Vercel.
        configuration.setAllowedOrigins(List.of("*")); 
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"));
        configuration.setExposedHeaders(Arrays.asList("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}