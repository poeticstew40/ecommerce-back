package back.ecommerce.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import back.ecommerce.dtos.DireccionRequest;
import back.ecommerce.dtos.DireccionResponse;
import back.ecommerce.entities.DireccionEntity;
import back.ecommerce.entities.UsuariosEntity;
import back.ecommerce.repositories.DireccionRepository;
import back.ecommerce.repositories.UsuariosRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class DireccionServiceImpl implements DireccionService {

    private final DireccionRepository direccionRepository;
    private final UsuariosRepository usuariosRepository;

    @Override
    public DireccionResponse create(DireccionRequest request) {
        UsuariosEntity usuario = usuariosRepository.findById(request.getUsuarioDni())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con DNI: " + request.getUsuarioDni()));

        DireccionEntity entity = new DireccionEntity();
        entity.setCalle(request.getCalle());
        entity.setNumero(request.getNumero());
        entity.setPiso(request.getPiso());
        entity.setDepartamento(request.getDepartamento());
        entity.setLocalidad(request.getLocalidad());
        entity.setProvincia(request.getProvincia());
        entity.setCodigoPostal(request.getCodigoPostal());
        entity.setUsuario(usuario);

        DireccionEntity guardada = direccionRepository.save(entity);
        return convertirAResponse(guardada);
    }

    @Override
    public List<DireccionResponse> readAllByUsuario(Long dni) {
        return direccionRepository.findByUsuarioDni(dni).stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    @Override
    public DireccionResponse readById(Long id) {
        DireccionEntity entity = direccionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dirección no encontrada con ID: " + id));
        return convertirAResponse(entity);
    }

    @Override
    public void delete(Long id) {
        if (!direccionRepository.existsById(id)) {
            throw new IllegalArgumentException("Dirección no encontrada con ID: " + id);
        }
        direccionRepository.deleteById(id);
    }

    private DireccionResponse convertirAResponse(DireccionEntity entity) {
        return DireccionResponse.builder()
                .id(entity.getId())
                .calle(entity.getCalle())
                .numero(entity.getNumero())
                .piso(entity.getPiso())
                .departamento(entity.getDepartamento())
                .localidad(entity.getLocalidad())
                .provincia(entity.getProvincia())
                .codigoPostal(entity.getCodigoPostal())
                .usuarioDni(entity.getUsuario().getDni())
                .build();
    }
}