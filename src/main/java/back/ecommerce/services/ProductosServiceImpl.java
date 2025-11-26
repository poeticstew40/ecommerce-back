package back.ecommerce.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import back.ecommerce.dtos.ProductosRequest;
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
    private final TiendaRepository tiendaRepository; 
    private final CloudinaryService cloudinaryService;

    @Override
    public ProductosResponse create(String nombreTienda, ProductosRequest productoRequest) {
        // Método create sin archivo (sobrecarga opcional)
        return create(nombreTienda, productoRequest, null);
    }

    @Override
    public ProductosResponse create(String nombreTienda, ProductosRequest productoRequest, MultipartFile file) {
        
        var tienda = tiendaRepository.findByNombreUrl(nombreTienda)
                .orElseThrow(() -> new IllegalArgumentException("Tienda no encontrada: " + nombreTienda));

        var categoria = categoriasRepository.findById(productoRequest.getCategoriaId())
            .orElseThrow(() -> new IllegalArgumentException("Categoria no encontrada con id: " + productoRequest.getCategoriaId()));

        if (!categoria.getTienda().getId().equals(tienda.getId())) {
            throw new IllegalArgumentException("Error de Seguridad: La categoría no pertenece a esta tienda.");
        }

        String urlImagen = null;
        if (file != null && !file.isEmpty()) {
            urlImagen = cloudinaryService.uploadFile(file);
        } else {
            // Imagen por defecto si es necesario
            urlImagen = "https://res.cloudinary.com/dacnqinsu/image/upload/v1/default-product.png";
        }

        var entity = new ProductosEntity();
        BeanUtils.copyProperties(productoRequest, entity);
        
        entity.setImagen(urlImagen);
        entity.setCategoria(categoria);
        entity.setTienda(tienda);

        var productoCreated = productosRepository.save(entity);

        return convertirEntidadAResponse(productoCreated);
    }

    @Override
    public List<ProductosResponse> readAllByTienda(String nombreTienda, String orden) {
        
        Sort sort = Sort.by("id").descending();

        if (orden != null) {
            switch (orden) {
                case "precio_asc":
                    sort = Sort.by("precio").ascending();
                    break;
                case "precio_desc":
                    sort = Sort.by("precio").descending();
                    break;
                case "nombre_asc":
                    sort = Sort.by("nombre").ascending();
                    break;
                case "nombre_desc":
                    sort = Sort.by("nombre").descending();
                    break;
            }
        }

        return productosRepository.findByTiendaNombreUrl(nombreTienda, sort)
                .stream()
                .map(this::convertirEntidadAResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductosResponse> buscarPorNombre(String nombreTienda, String termino) {
        return productosRepository.findByTiendaNombreUrlAndNombreContainingIgnoreCase(nombreTienda, termino)
                .stream()
                .map(this::convertirEntidadAResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductosResponse> buscarPorCategoria(String nombreTienda, Long categoriaId) {
        return productosRepository.findByTiendaNombreUrlAndCategoriaId(nombreTienda, categoriaId)
                .stream()
                .map(this::convertirEntidadAResponse)
                .collect(Collectors.toList());
    }

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
        
        if (productoRequest.getCategoriaId() != null) {
            var categoria = categoriasRepository.findById(productoRequest.getCategoriaId())
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con id: " + productoRequest.getCategoriaId()));

            if (!categoria.getTienda().getId().equals(entityFromDB.getTienda().getId())) {
                 throw new IllegalArgumentException("Error: No puedes mover este producto a una categoría de otra tienda.");
            }
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