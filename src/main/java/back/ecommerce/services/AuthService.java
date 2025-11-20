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
        // ✅ VALIDACIÓN 1: Chequear si el DNI ya existe
        if (usuariosRepository.existsById(request.getDni())) {
            throw new IllegalArgumentException("Ya existe un usuario con el DNI " + request.getDni());
        }

        // ✅ VALIDACIÓN 2: Chequear si el Email ya existe
        if (usuariosRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("El email " + request.getEmail() + " ya está registrado");
        }

        // Si pasa las validaciones, creamos el usuario
        var user = UsuariosEntity.builder()
                .dni(request.getDni())
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .rol(Rol.COMPRADOR)
                .build();

        usuariosRepository.save(user);

        var jwtToken = jwtService.generateToken(user);
        
        return AuthResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var user = usuariosRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Usuario o contraseña incorrectos"));

        var jwtToken = jwtService.generateToken(user);
        
        return AuthResponse.builder()
                .token(jwtToken)
                .build();
    }
}