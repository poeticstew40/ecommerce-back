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
                        .version("1.0.0")
                        .description("""
                                # Guía de Uso para Frontend

                                documentación de la API REST del E-commerce Multitienda.
                                A continuación se detalla el flujo de trabajo para integrar esta API.

                                ---

                                ## 1. Autenticación y Seguridad (JWT)
                                La mayoría de los endpoints están protegidos. Sigue estos pasos para probarlos:
                                1. Ve al controlador **Auth Controller**.
                                2. Usa el endpoint `/api/auth/register` para crear un usuario o `/api/auth/login` si ya tienes uno.
                                3. Copia el `token` que recibes en la respuesta JSON.
                                4. Ve a la parte superior de esta página y haz clic en el botón **Authorize** (el candado).
                                5. En el campo de texto, escribe la palabra `Bearer` seguida de un espacio y pega tu token.
                                   Ejemplo: `Bearer eyJhbGciOiJIUzI1NiJ9...`
                                6. Haz clic en **Authorize**. Ahora estás logueado.

                                ---

                                ## 2. Arquitectura Multitienda (Slug)
                                Este sistema separa los recursos por tiendas utilizando un identificador único en la URL (slug).
                                * **Patrón de URL:** `/api/tiendas/{nombreTienda}/...`
                                * **Importante:** Si intentas acceder a productos de la tienda 'nike' usando la URL de la tienda 'adidas', la API devolverá un error de seguridad.
                                * Asegúrate de usar siempre el `nombreUrl` correcto que obtienes al crear la tienda.

                                ---

                                ## 3. Gestión de Imágenes (Multipart File)
                                * **Productos y Tiendas:** Los endpoints de creación (`POST`) consumen `multipart/form-data`.
                                * En Swagger, verás un campo `file` para seleccionar la imagen desde tu computadora.
                                * El campo `producto` (o `tienda`) debe enviarse como `application/json`. Swagger se encarga de esto automáticamente, pero si usas Postman, debes configurar el `Content-Type` manualmente en esa parte del form-data.

                                ---

                                ## 4. Flujo de Compra
                                1. **Carrito:** Agrega items usando `/api/tiendas/{nombre}/carrito/agregar`.
                                2. **Checkout:** Crea un pedido enviando un POST a `/api/tiendas/{nombre}/pedidos`. Si envías la lista de items vacía `[]`, el sistema procesará automáticamente lo que haya en el carrito del usuario.
                                3. **Pago:** Usa el ID del pedido generado para llamar a `/api/pagos/crear/{id}`. Esto devolverá la URL de Mercado Pago.

                                ---

                                **Nota:** Si recibes un error 403 Forbidden, verifica que tu token no haya expirado y que hayas incluido el prefijo 'Bearer '.
                                """))
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