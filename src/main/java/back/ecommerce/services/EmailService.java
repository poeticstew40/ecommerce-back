package back.ecommerce.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailService {

    @Value("${resend.api.key}")
    private String resendApiKey;

    // Si ya verificaste tu dominio, ponlo acá. Si no, usa "onboarding@resend.dev"
    private final String REMITENTE = "Ecommerce <hola@tudominio.com>"; 

    @Async
    public void enviarCorreo(String to, String subject, String body) {
        try {
            Resend resend = new Resend(resendApiKey);

            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(REMITENTE)
                    .to(to)
                    .subject(subject)
                    .html(body.replace("\n", "<br>")) 
                    .build();

            resend.emails().send(params);
            log.info("✅ Email enviado a: {}", to);

        } catch (Exception e) {
            log.error("❌ Error enviando email: {}", e.getMessage());
        }
    }
}