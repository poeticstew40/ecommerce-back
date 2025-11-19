package back.ecommerce.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import back.ecommerce.dtos.ProductosRequest; // 👈 Importante
import back.ecommerce.dtos.ProductosResponse;
import back.ecommerce.entities.ProductosEntity;
import back.ecommerce.repositories.CategoriasRepository;
import back.ecommerce.repositories.ProductosRepository;
import back.ecommerce.repositories.TiendaRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
@AllArgsConstructor
public class ProductosServiceImpl implements ProductosService {

    private final ProductosRepository productosRepository;
    private final CategoriasRepository categoriasRepository;
    private final TiendaRepository tiendaRepository; // 👈 Nuevo repo inyectado

    @Override
    public ProductosResponse create(String nombreTienda, ProductosRequest productoRequest) {
        // 1. Buscamos la TIENDA
        var tienda = tiendaRepository.findByNombreUrl(nombreTienda)
                .orElseThrow(() -> new IllegalArgumentException("Tienda no encontrada: " + nombreTienda));

        // 2. Creamos la entidad
        var entity = new ProductosEntity();
        BeanUtils.copyProperties(productoRequest, entity);

        // 3. Buscamos y asignamos la categoría
        var categoria = categoriasRepository.findById(productoRequest.getCategoriaId())
            .orElseThrow(() -> new IllegalArgumentException("Categoria no encontrada con id: " + productoRequest.getCategoriaId()));
        entity.setCategoria(categoria);

        // 4. Asignamos la tienda a la entidad
        entity.setTienda(tienda); 

        // 5. Guardamos
        var productoCreated = productosRepository.save(entity);

        return convertirEntidadAResponse(productoCreated);
    }

    @Override
    public List<ProductosResponse> readAllByTienda(String nombreTienda) {
        // Usamos el método del repositorio que filtra por la URL de la tienda
        return productosRepository.findByTiendaNombreUrl(nombreTienda)
                .stream()
                .map(this::convertirEntidadAResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductosResponse> buscarPorNombre(String nombreTienda, String termino) {
        // Buscamos por nombre PERO solo dentro de esa tienda
        return productosRepository.findByTiendaNombreUrlAndNombreContainingIgnoreCase(nombreTienda, termino)
                .stream()
                .map(this::convertirEntidadAResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductosResponse> buscarPorCategoria(String nombreTienda, Long categoriaId) {
        // Filtramos por categoría PERO solo dentro de esa tienda
        return productosRepository.findByTiendaNombreUrlAndCategoriaId(nombreTienda, categoriaId)
                .stream()
                .map(this::convertirEntidadAResponse)
                .collect(Collectors.toList());
    }

    // --- Métodos por ID (Mantenemos la lógica original) ---

    @Override
    public ProductosResponse readById(Long id) {
        final var entityResponse = this.productosRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con id: " + id));
        return convertirEntidadAResponse(entityResponse);
    }

    @Override
    public ProductosResponse update(Long id, ProductosRequest productoRequest) {
        final var entityFromDB = this.productosRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con id: " + id));

        if (productoRequest.getNombre() != null && !productoRequest.getNombre().isBlank()) {
            entityFromDB.setNombre(productoRequest.getNombre());
        }
        if (productoRequest.getDescripcion() != null) {
            entityFromDB.setDescripcion(productoRequest.getDescripcion());
        }
        if (productoRequest.getPrecio() != null) {
            entityFromDB.setPrecio(productoRequest.getPrecio());
        }
        if (productoRequest.getStock() != null) {
            entityFromDB.setStock(productoRequest.getStock());
        }
        if (productoRequest.getImagen() != null) {
            entityFromDB.setImagen(productoRequest.getImagen());
        }
        if (productoRequest.getCategoriaId() != null) {
            var categoria = categoriasRepository.findById(productoRequest.getCategoriaId())
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con id: " + productoRequest.getCategoriaId()));
            entityFromDB.setCategoria(categoria);
        }
        
        var productoActualizado = this.productosRepository.save(entityFromDB);
        return convertirEntidadAResponse(productoActualizado);
    }

    @Override
    public void delete(Long id) {
        var producto = this.productosRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con id: " + id));
        
        log.info("Eliminando producto: {}", producto.getNombre());
        this.productosRepository.delete(producto);
    }

    // --- Helper ---
    private ProductosResponse convertirEntidadAResponse(ProductosEntity entidad) {
        var response = new ProductosResponse();
        BeanUtils.copyProperties(entidad, response);

        if (entidad.getCategoria() != null) {
            response.setCategoriaId(entidad.getCategoria().getId());
            response.setCategoriaNombre(entidad.getCategoria().getNombre());
        }
        
        return response;
    }
}