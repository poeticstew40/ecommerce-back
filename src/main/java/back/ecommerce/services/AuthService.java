package back.ecommerce.services;

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

    public AuthResponse register(RegisterRequest request) {
        // 1. Crear el usuario (encriptando la clave)
        var user = UsuariosEntity.builder()
                .dni(request.getDni())
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // ¡Clave encriptada!
                .rol(Rol.COMPRADOR) // Por defecto, todos son compradores al registrarse
                .build();

        // 2. Guardar en DB
        usuariosRepository.save(user);

        // 3. Generar Token
        var jwtToken = jwtService.generateToken(user);
        
        return AuthResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthResponse login(AuthRequest request) {
        // 1. Autenticar (esto valida usuario y contraseña automáticamente)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. Si pasó la autenticación, buscamos al usuario
        var user = usuariosRepository.findByEmail(request.getEmail())
                .orElseThrow();

        // 3. Generamos un nuevo token
        var jwtToken = jwtService.generateToken(user);
        
        return AuthResponse.builder()
                .token(jwtToken)
                .build();
    }
}