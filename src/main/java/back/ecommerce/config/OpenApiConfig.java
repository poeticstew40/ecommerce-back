package back.ecommerce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API E-commerce Multitienda")
                        .version("1.0")
                        .description("Documentación técnica para integración Frontend-Backend.\n\n" +
                                     "Instrucciones de uso:\n" +
                                     "1. Utilice el endpoint de Login para obtener el token JWT.\n" +
                                     "2. Haga clic en el botón 'Authorize' (candado) e ingrese el token con el formato: Bearer {token}.\n" +
                                     "3. Todas las rutas de la tienda siguen el patrón: /api/tiendas/{nombreTienda}/..."))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()));
    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer");
    }
}