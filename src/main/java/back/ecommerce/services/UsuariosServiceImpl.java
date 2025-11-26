package back.ecommerce.services;

import java.util.List;
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

        if (request.getNombre() != null && !request.getNombre().isBlank()) {
            entidad.setNombre(request.getNombre());
        }
        if (request.getApellido() != null && !request.getApellido().isBlank()) {
            entidad.setApellido(request.getApellido());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            entidad.setEmail(request.getEmail());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            entidad.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        var usuarioActualizado = usuariosRepository.save(entidad);
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
                .build();
    }
}