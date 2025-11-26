package back.ecommerce.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final TiendaRepository tiendaRepository;

    @Override
    public CategoriasResponse create(String nombreTienda, CategoriasRequest request) {
        var tienda = tiendaRepository.findByNombreUrl(nombreTienda)
                .orElseThrow(() -> new IllegalArgumentException("Tienda no encontrada: " + nombreTienda));
        
        // Validación de duplicados
        boolean existe = categoriasRepository.findByTiendaNombreUrl(nombreTienda).stream()
                .anyMatch(cat -> cat.getNombre().equalsIgnoreCase(request.getNombre()));
        
        if (existe) {
            throw new IllegalArgumentException("Ya existe una categoría con el nombre '" + request.getNombre() + "' en esta tienda.");
        }

        var entity = new CategoriasEntity();
        entity.setNombre(request.getNombre());
        entity.setTienda(tienda);
        
        var categoriaGuardada = categoriasRepository.save(entity);
        return convertirEntidadAResponse(categoriaGuardada);
    }

    @Override
    public List<CategoriasResponse> readAllByTienda(String nombreTienda) {
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

    private CategoriasResponse convertirEntidadAResponse(CategoriasEntity entity) {
        var response = new CategoriasResponse();
        BeanUtils.copyProperties(entity, response);
        return response;
    }
}