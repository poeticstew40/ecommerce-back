package back.ecommerce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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
            // 1. Desactivar CSRF (No es necesario para APIs REST Stateless)
            .csrf(AbstractHttpConfigurer::disable)
            
            // 2. Configurar Permisos de Rutas
            .authorizeHttpRequests(auth -> auth
                // 1. Rutas 100% Públicas (Login, Registro, Docs, Webhooks)
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/api/pagos/**").permitAll()
                .requestMatchers("/api/pagos/webhook").permitAll()

                // 2. La "Vidriera": CUALQUIERA puede VER tiendas y productos (GET)
                .requestMatchers(HttpMethod.GET, "/tiendas/**").permitAll()

                // 3. El "Mostrador": SOLO usuarios autenticados pueden CREAR tiendas (POST)
                // Esta línea es implícita por el anyRequest().authenticated(), 
                // pero el problema es que si tu token no es válido o el filtro falla, te rebota.
                
                // 4. Todo lo demás requiere autenticación
                .anyRequest().authenticated()
            )

            // 3. Configurar Sesión (Stateless = Sin estado, cada petición es nueva)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // 4. Agregar nuestro AuthenticationProvider (BCrypt + UserDetails)
            .authenticationProvider(authenticationProvider)

            // 5. Agregar el Filtro JWT antes del filtro de usuario/password estándar
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}