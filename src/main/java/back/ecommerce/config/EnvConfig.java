package back.ecommerce.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;
import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
public class EnvConfig {

    static {
        String[] posiblesRutas = { "./", "./Backend" };
        boolean cargado = false;

        for (String ruta : posiblesRutas) {
            if (Files.exists(Paths.get(ruta, ".env"))) {
                try {
                    Dotenv dotenv = Dotenv.configure()
                            .directory(ruta)
                            .ignoreIfMissing()
                            .load();

                    dotenv.entries().forEach(entry -> {
                        System.setProperty(entry.getKey(), entry.getValue());
                    });

                    System.out.println("✅ Variables de entorno cargadas desde: " + ruta);
                    cargado = true;
                    break; 
                } catch (Exception e) {
                    System.out.println("⚠️ Error leyendo .env en " + ruta + ": " + e.getMessage());
                }
            }
        }

        if (!cargado) {
            System.out.println("ℹ️ No se encontró archivo .env local. Usando variables de entorno del sistema.");
        }
    }
}