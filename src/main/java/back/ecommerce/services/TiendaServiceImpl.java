package back.ecommerce.services;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import back.ecommerce.dtos.TiendaRequest;
import back.ecommerce.dtos.TiendaResponse;
import back.ecommerce.entities.TiendaEntity;
import back.ecommerce.repositories.TiendaRepository;
import back.ecommerce.repositories.UsuariosRepository;
import lombok.AllArgsConstructor;

@Service
@Transactional
@AllArgsConstructor
public class TiendaServiceImpl implements TiendaService {

    private final TiendaRepository tiendaRepository;
    private final UsuariosRepository usuariosRepository;

    @Override
    public TiendaResponse create(TiendaRequest request) {
        // 1. Validar que el vendedor exista
        var vendedor = usuariosRepository.findById(request.getVendedorDni())
                .orElseThrow(() -> new IllegalArgumentException("Vendedor no encontrado con DNI: " + request.getVendedorDni()));

        // 2. Validar que el slug (URL) no esté ocupado
        if (tiendaRepository.findByNombreUrl(request.getNombreUrl()).isPresent()) {
            throw new IllegalArgumentException("La URL de tienda '" + request.getNombreUrl() + "' ya está en uso.");
        }

        // 3. Crear entidad
        var entity = new TiendaEntity();
        BeanUtils.copyProperties(request, entity);
        entity.setVendedor(vendedor);

        // 4. Guardar
        var tiendaGuardada = tiendaRepository.save(entity);
        
        return convertirEntidadAResponse(tiendaGuardada);
    }

    @Override
    public TiendaResponse readByNombreUrl(String nombreUrl) {
        var entity = tiendaRepository.findByNombreUrl(nombreUrl)
                .orElseThrow(() -> new IllegalArgumentException("Tienda no encontrada: " + nombreUrl));
        
        return convertirEntidadAResponse(entity);
    }

    private TiendaResponse convertirEntidadAResponse(TiendaEntity entity) {
        return TiendaResponse.builder()
                .id(entity.getId())
                .nombreUrl(entity.getNombreUrl())
                .nombreFantasia(entity.getNombreFantasia())
                .logo(entity.getLogo())
                .descripcion(entity.getDescripcion())
                .vendedorDni(entity.getVendedor() != null ? entity.getVendedor().getDni() : null)
                .vendedorNombre(entity.getVendedor() != null ? entity.getVendedor().getNombre() : null)
                .build();
    }
}