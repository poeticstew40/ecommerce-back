package back.ecommerce.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 👈 Importante

import back.ecommerce.dtos.CategoriasRequest;
import back.ecommerce.dtos.CategoriasResponse;
import back.ecommerce.entities.CategoriasEntity;
import back.ecommerce.repositories.CategoriasRepository;
import back.ecommerce.repositories.TiendaRepository;
import lombok.AllArgsConstructor;

@Service
@Transactional
@AllArgsConstructor
public class CategoriasServiceImpl implements CategoriasService {

    private final CategoriasRepository categoriasRepository;
    private final TiendaRepository tiendaRepository; // 👈 Inyección del repo de tiendas

    @Override
    public CategoriasResponse create(String nombreTienda, CategoriasRequest request) {
        // 1. Buscamos la TIENDA por su slug (URL)
        var tienda = tiendaRepository.findByNombreUrl(nombreTienda)
                .orElseThrow(() -> new IllegalArgumentException("Tienda no encontrada: " + nombreTienda));

        // 2. Creamos la entidad Categoría
        var entity = new CategoriasEntity();
        entity.setNombre(request.getNombre());
        
        // 3. La vinculamos a la tienda
        entity.setTienda(tienda);

        // 4. Guardamos
        var categoriaGuardada = categoriasRepository.save(entity);
        
        return convertirEntidadAResponse(categoriaGuardada);
    }

    @Override
    public List<CategoriasResponse> readAllByTienda(String nombreTienda) {
        // Usamos el método del repositorio que filtra por tienda
        return categoriasRepository.findByTiendaNombreUrl(nombreTienda).stream()
                .map(this::convertirEntidadAResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CategoriasResponse readById(Long id) {
        var entity = categoriasRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con id: " + id));
        return convertirEntidadAResponse(entity);
    }

    @Override
    public CategoriasResponse update(Long id, CategoriasRequest request) {
        var entity = categoriasRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con id: " + id));

        if (request.getNombre() != null && !request.getNombre().isBlank()) {
            entity.setNombre(request.getNombre());
        }

        var categoriaActualizada = categoriasRepository.save(entity);
        return convertirEntidadAResponse(categoriaActualizada);
    }

    @Override
    public void delete(Long id) {
        var entity = categoriasRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con id: " + id));
        categoriasRepository.delete(entity);
    }

    // --- Helper para convertir Entidad a DTO ---
    private CategoriasResponse convertirEntidadAResponse(CategoriasEntity entity) {
        var response = new CategoriasResponse();
        BeanUtils.copyProperties(entity, response);
        return response;
    }
}