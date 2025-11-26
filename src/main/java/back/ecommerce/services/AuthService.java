package back.ecommerce.services;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import back.ecommerce.dtos.AuthRequest;
import back.ecommerce.dtos.AuthResponse;
import back.ecommerce.dtos.RegisterRequest;
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

    @Value("${app.backend.url}")
    private String backendUrl;

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

        String link = backendUrl + "/api/auth/verify?code=" + codigoVerificacion;
        
        String mensaje = "Hola " + user.getNombre() + "!\n\n" +
                         "Gracias por registrarte. Para activar tu cuenta, hacé clic en el siguiente enlace:\n\n" +
                         link + "\n\n" +
                         "Si no solicitaste esto, ignorá este mensaje.";
        
        emailService.enviarCorreo(user.getEmail(), "Verificá tu cuenta", mensaje);

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
}