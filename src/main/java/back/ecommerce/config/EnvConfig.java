package back.ecommerce.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnvConfig {

    static {
        // Bloque estático: Se ejecuta apenas carga la clase, antes que Spring lea el properties
        try {
            Dotenv dotenv = Dotenv.configure()
                    .directory("./") // Busca en la raíz del proyecto
                    .ignoreIfMissing() // En prod (Render/Google) no existe el archivo, así que no falla
                    .load();

            dotenv.entries().forEach(entry -> {
                System.setProperty(entry.getKey(), entry.getValue());
            });
            System.out.println(" Variables de entorno cargadas desde .env local");
        } catch (Exception e) {
            System.out.println(" No se encontró archivo .env, asumiendo variables de entorno del sistema (Producción)");
        }
    }
}