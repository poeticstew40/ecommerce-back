package back.ecommerce.services;

import java.util.UUID;

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
import lombok.RequiredArgsConstructor; // Para generar el código único

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuariosRepository usuariosRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService; // ✅ Inyectamos EmailService

    public AuthResponse register(RegisterRequest request) {
        if (usuariosRepository.existsById(request.getDni())) {
            throw new IllegalArgumentException("Ya existe un usuario con el DNI " + request.getDni());
        }
        if (usuariosRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("El email " + request.getEmail() + " ya está registrado");
        }

        // Generamos código de verificación
        String codigoVerificacion = UUID.randomUUID().toString();

        var user = UsuariosEntity.builder()
                .dni(request.getDni())
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .rol(Rol.COMPRADOR)
                .emailVerificado(false) // ✅ Nace NO verificado
                .verificationCode(codigoVerificacion) // ✅ Guardamos el código
                .build();

        usuariosRepository.save(user);

        // ✅ ENVIAR EL MAIL
        // (En prod esto sería la URL de tu frontend o backend en Render)
        String link = "http://localhost:8080/ecommerce/auth/verify?code=" + codigoVerificacion;
        
        String mensaje = "Hola " + user.getNombre() + "!\n\n" +
                         "Para activar tu cuenta de vendedor, hacé clic acá:\n" +
                         link + "\n\n" +
                         "Si no solicitaste esto, ignorá este mensaje.";

        emailService.enviarCorreo(user.getEmail(), "Verificá tu cuenta", mensaje);

        // Retornamos token (aunque no pueda crear tiendas todavía, puede loguearse)
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

    // ✅ NUEVO MÉTODO PARA VALIDAR
    public String verifyUser(String code) {
        UsuariosEntity user = usuariosRepository.findByVerificationCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Código de verificación inválido o expirado"));

        if (user.isEmailVerificado()) {
            return "Tu cuenta ya estaba verificada.";
        }

        user.setEmailVerificado(true);
        user.setVerificationCode(null); // Borramos el código por seguridad
        usuariosRepository.save(user);

        return "¡Cuenta verificada con éxito! Ahora podés crear tu tienda.";
    }
}