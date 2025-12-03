package back.ecommerce.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailService {

    @Value("${resend.api.key:}") 
    private String resendApiKey;

    @Value("${resend.email.from:Ecommerce <info@nicolasgigena.com.ar>}")
    private String remitente;

    @PostConstruct
    public void init() {
        if (resendApiKey == null || resendApiKey.isBlank()) {
            log.warn("⚠️ ADVERTENCIA: La API Key de Resend no está configurada. Los correos NO se enviarán (solo se verán en consola).");
        } else {
            log.info("✅ Servicio de Email iniciado correctamente. Enviando desde: {}", remitente);
        }
    }

    @Async
    public void enviarCorreo(String to, String subject, String htmlBody) {
        if (resendApiKey != null && !resendApiKey.isBlank()) {
            try {
                Resend resend = new Resend(resendApiKey);

                CreateEmailOptions params = CreateEmailOptions.builder()
                        .from(remitente)
                        .to(to)
                        .subject(subject)
                        .html(htmlBody)
                        .build();

                resend.emails().send(params);
                log.info("📧 Email enviado a: {} | Asunto: {}", to, subject);
                return; 

            } catch (Exception e) {
                log.error("❌ FALLÓ EL ENVÍO REAL A RESEND: {}", e.getMessage());
            }
        }

        imprimirEmailEnConsola(to, subject, htmlBody);
    }

    private void imprimirEmailEnConsola(String to, String subject, String body) {
        System.out.println("\n================ [ SIMULACIÓN DE EMAIL ] ================");
        System.out.println("DE:      " + remitente);
        System.out.println("PARA:    " + to);
        System.out.println("ASUNTO:  " + subject);
        System.out.println("---------------------------------------------------------");
        
        String textoPlano = body.replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").trim();
        
        if(textoPlano.contains("http")) {
            int start = textoPlano.indexOf("http");
            int end = textoPlano.indexOf(" ", start);
            if (end == -1) end = textoPlano.length();
            System.out.println("🔗 LINK DETECTADO: " + textoPlano.substring(start, end));
        }
        
        System.out.println("CONTENIDO (HTML Oculto): " + (body.length() > 50 ? body.substring(0, 50) + "..." : body));
        System.out.println("=========================================================\n");
    }
}