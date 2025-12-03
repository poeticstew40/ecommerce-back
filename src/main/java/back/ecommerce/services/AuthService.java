package back.ecommerce.services;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import back.ecommerce.dtos.AuthRequest;
import back.ecommerce.dtos.AuthResponse;
import back.ecommerce.dtos.ChangePasswordRequest;
import back.ecommerce.dtos.ForgotPasswordRequest;
import back.ecommerce.dtos.RegisterRequest;
import back.ecommerce.dtos.ResetPasswordRequest;
import back.ecommerce.entities.Rol;
import back.ecommerce.entities.UsuariosEntity;
import back.ecommerce.repositories.UsuariosRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuariosRepository usuariosRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Value("${app.backend.url:http://localhost:8080}")
    private String backendUrl;
    
    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    public AuthResponse register(RegisterRequest request) {
        if (usuariosRepository.existsById(request.getDni())) {
            throw new IllegalArgumentException("Ya existe un usuario con el DNI " + request.getDni());
        }
        if (usuariosRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("El email " + request.getEmail() + " ya está registrado");
        }

        String codigoVerificacion = UUID.randomUUID().toString();
        var user = UsuariosEntity.builder()
                .dni(request.getDni())
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .rol(Rol.COMPRADOR)
                .emailVerificado(false)
                .verificationCode(codigoVerificacion)
                .build();

        usuariosRepository.save(user);
        enviarEmailVerificacion(user);

        var jwtToken = jwtService.generateToken(user);
        return AuthResponse.builder().token(jwtToken).build();
    }

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        var user = usuariosRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Usuario o contraseña incorrectos"));
        var jwtToken = jwtService.generateToken(user);
        return AuthResponse.builder().token(jwtToken).build();
    }

    public String verifyUser(String code) {
        UsuariosEntity user = usuariosRepository.findByVerificationCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Código de verificación inválido o expirado"));

        if (user.isEmailVerificado()) {
            return "Tu cuenta ya estaba verificada. Puedes iniciar sesión.";
        }

        user.setEmailVerificado(true);
        user.setVerificationCode(null);
        usuariosRepository.save(user);
        return "¡Cuenta verificada con éxito! Ya podés cerrar esta ventana e iniciar sesión.";
    }

    public String forgotPassword(ForgotPasswordRequest request) {
        var user = usuariosRepository.findById(request.getDni())
                .orElseThrow(() -> new IllegalArgumentException("No se encontró usuario con ese DNI"));

        if (!user.getEmail().equalsIgnoreCase(request.getEmail())) {
            throw new IllegalArgumentException("El email ingresado no coincide con el registrado para este DNI");
        }

        String token = UUID.randomUUID().toString();
        user.setPasswordResetToken(token);
        usuariosRepository.save(user);

        String link = frontendUrl + "/reset-password?token=" + token;
        
        // HTML Template para Recuperar Clave
        String html = """
            <html>
            <body style='font-family: Arial, sans-serif; color: #333; background-color: #f4f4f4; padding: 20px;'>
                <div style='max-width: 600px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>
                    <h2 style='color: #4F46E5; text-align: center;'>Recuperación de Contraseña</h2>
                    <p>Hola <strong>%s</strong>,</p>
                    <p>Recibimos una solicitud para restablecer tu contraseña. Si fuiste vos, hacé clic en el botón de abajo:</p>
                    <div style='text-align: center; margin: 30px 0;'>
                        <a href='%s' style='background-color: #4F46E5; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; font-weight: bold;'>Restablecer Contraseña</a>
                    </div>
                    <p style='font-size: 12px; color: #666;'>Si no solicitaste este cambio, podés ignorar este correo de forma segura.</p>
                </div>
            </body>
            </html>
            """.formatted(user.getNombre(), link);
        
        emailService.enviarCorreo(user.getEmail(), "Recuperar Contraseña", html);
        
        return "Te enviamos un email para restablecer tu contraseña.";
    }

    public String resetPassword(ResetPasswordRequest request) {
        var user = usuariosRepository.findByPasswordResetToken(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Token inválido o expirado"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordResetToken(null);
        usuariosRepository.save(user);

        enviarAlertaSeguridad(user, "Tu contraseña ha sido restablecida exitosamente.");

        return "Contraseña actualizada correctamente.";
    }

    public String changePassword(ChangePasswordRequest request) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        if (!(principal instanceof UsuariosEntity)) {
             throw new IllegalArgumentException("No se pudo identificar al usuario actual.");
        }
        
        UsuariosEntity usuarioLogueado = (UsuariosEntity) principal;

        if (!passwordEncoder.matches(request.getCurrentPassword(), usuarioLogueado.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        }

        if (passwordEncoder.matches(request.getNewPassword(), usuarioLogueado.getPassword())) {
             throw new IllegalArgumentException("La nueva contraseña no puede ser igual a la actual");
        }

        usuarioLogueado.setPassword(passwordEncoder.encode(request.getNewPassword()));
        usuariosRepository.save(usuarioLogueado);

        enviarAlertaSeguridad(usuarioLogueado, "Acabas de cambiar tu contraseña desde tu perfil.");

        return "Contraseña actualizada exitosamente";
    }

    public void enviarEmailVerificacion(UsuariosEntity user) {
        String link = backendUrl + "/api/auth/verify?code=" + user.getVerificationCode();
        
        // HTML Template para Verificación con Botón
        String html = """
            <html>
            <body style='font-family: Arial, sans-serif; color: #333; background-color: #f4f4f4; padding: 20px;'>
                <div style='max-width: 600px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>
                    <div style='text-align: center; border-bottom: 2px solid #4F46E5; padding-bottom: 10px; margin-bottom: 20px;'>
                        <h1 style='color: #4F46E5; margin: 0;'>Bienvenido!</h1>
                    </div>
                    <p>Hola <strong>%s</strong>,</p>
                    <p>Gracias por registrarte. Para activar tu cuenta y empezar a comprar o vender, por favor verificá tu email:</p>
                    <div style='text-align: center; margin: 30px 0;'>
                        <a href='%s' style='background-color: #22c55e; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; font-weight: bold; font-size: 16px;'>Verificar mi Cuenta</a>
                    </div>
                    <p style='font-size: 12px; color: #666;'>Si el botón no funciona, copia y pega este enlace: <br> %s</p>
                </div>
            </body>
            </html>
            """.formatted(user.getNombre(), link, link);

        emailService.enviarCorreo(user.getEmail(), "Verificá tu cuenta", html);
    }

    private void enviarAlertaSeguridad(UsuariosEntity user, String detalle) {
        String html = """
            <html>
            <body style='font-family: Arial, sans-serif; color: #333; background-color: #fff0f0; padding: 20px;'>
                <div style='max-width: 600px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; border-left: 5px solid #dc2626; box-shadow: 0 2px 4px rgba(0,0,0,0.1);'>
                    <h3 style='color: #dc2626;'>Alerta de Seguridad 🔒</h3>
                    <p>Hola <strong>%s</strong>,</p>
                    <p>%s</p>
                    <p>Si fuiste vos, no es necesario hacer nada.</p>
                    <p style='font-weight: bold;'>Si NO fuiste vos, contacta a soporte inmediatamente.</p>
                </div>
            </body>
            </html>
            """.formatted(user.getNombre(), detalle);
            
        emailService.enviarCorreo(user.getEmail(), "Aviso de Seguridad", html);
    }
}