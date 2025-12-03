package back.ecommerce.services;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import back.ecommerce.dtos.UsuariosRequest;
import back.ecommerce.dtos.UsuariosResponse;
import back.ecommerce.entities.UsuariosEntity;
import back.ecommerce.repositories.UsuariosRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
@AllArgsConstructor
public class UsuariosServiceImpl implements UsuariosService {

    private final UsuariosRepository usuariosRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService; 
    private final EmailService emailService;

    @Override
    public List<UsuariosResponse> readAll() {
        return usuariosRepository.findAll().stream()
                .map(this::convertirEntidadAResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UsuariosResponse readByDni(Long dni) {
        final var entityResponse = this.usuariosRepository.findById(dni)
            .orElseThrow(() -> new IllegalArgumentException("No existe el usuario con id: " + dni));
        return convertirEntidadAResponse(entityResponse);
    }

    @Override
    public UsuariosResponse update(Long dni, UsuariosRequest request) {
        final var entidad = this.usuariosRepository.findById(dni)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con DNI: " + dni));

        boolean datosSensiblesCambiados = false;

        if (request.getNombre() != null && !request.getNombre().isBlank() && !request.getNombre().equals(entidad.getNombre())) {
            entidad.setNombre(request.getNombre());
            datosSensiblesCambiados = true;
        }
        
        if (request.getApellido() != null && !request.getApellido().isBlank() && !request.getApellido().equals(entidad.getApellido())) {
            entidad.setApellido(request.getApellido());
            datosSensiblesCambiados = true;
        }
        
        if (request.getEmail() != null && !request.getEmail().isBlank() && !request.getEmail().equalsIgnoreCase(entidad.getEmail())) {
            
            var posibleDueño = usuariosRepository.findByEmail(request.getEmail());
            
            if (posibleDueño.isPresent() && !posibleDueño.get().getDni().equals(dni)) {
                throw new IllegalArgumentException("El email " + request.getEmail() + " ya está en uso por otro usuario.");
            }
            
            entidad.setEmail(request.getEmail());
            entidad.setEmailVerificado(false);
            entidad.setVerificationCode(UUID.randomUUID().toString());
            
            authService.enviarEmailVerificacion(entidad); // Usa la plantilla HTML de AuthService
            log.info("Usuario {} cambió su email. Se envió correo de nueva verificación.", dni);
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            entidad.setPassword(passwordEncoder.encode(request.getPassword()));
            datosSensiblesCambiados = true;
        }

        var usuarioActualizado = usuariosRepository.save(entidad);

        if (datosSensiblesCambiados) {
            String html = """
                <html>
                <body style='font-family: Arial, sans-serif; color: #333;'>
                    <div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e5e7eb; border-radius: 8px;'>
                        <h3 style='color: #d97706;'>⚠️ Actualización de Perfil</h3>
                        <p>Hola <strong>%s</strong>,</p>
                        <p>Te informamos que tus datos personales o contraseña han sido modificados recientemente.</p>
                        <p style='font-size: 12px; color: #666; margin-top: 20px;'>Si no reconoces esta actividad, por favor recupera tu contraseña inmediatamente.</p>
                    </div>
                </body>
                </html>
                """.formatted(entidad.getNombre());

            emailService.enviarCorreo(entidad.getEmail(), "Actualización de Perfil", html);
        }

        return convertirEntidadAResponse(usuarioActualizado);
    }

    @Override
    public void delete(Long dni) {
        final var entidad = this.usuariosRepository.findById(dni)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con DNI: " + dni));
        this.usuariosRepository.delete(entidad);
    }

    private UsuariosResponse convertirEntidadAResponse(UsuariosEntity entidad) {
        return UsuariosResponse.builder()
                .dni(entidad.getDni())
                .nombre(entidad.getNombre())
                .apellido(entidad.getApellido())
                .email(entidad.getEmail())
                .emailVerificado(entidad.isEmailVerificado())
                .build();
    }
}