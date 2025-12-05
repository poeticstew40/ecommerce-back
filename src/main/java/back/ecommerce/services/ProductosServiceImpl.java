package back.ecommerce.services;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.Optional; // Asegúrate de que esta importación exista
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import back.ecommerce.dtos.ProductosRequest;
import back.ecommerce.dtos.ProductosResponse;
import back.ecommerce.entities.CategoriasEntity;
import back.ecommerce.entities.ProductosEntity;
import back.ecommerce.entities.TiendaEntity;
import back.ecommerce.entities.UsuariosEntity;
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
    public ProductosResponse create(String nombreTienda, ProductosRequest productoRequest, List<MultipartFile> files) {
        var tienda = tiendaRepository.findByNombreUrl(nombreTienda)
                .orElseThrow(() -> new IllegalArgumentException("Tienda no encontrada: " + nombreTienda));
        validarDueño(tienda);

        // --- INICIO MODIFICACIÓN CREATE: ASIGNACIÓN SEGURA DE CATEGORÍA ---
        var categoria = asignarCategoriaSegura(productoRequest.getCategoriaId(), tienda);
        // --- FIN MODIFICACIÓN CREATE ---

        List<String> listaImagenes = new ArrayList<>();
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String url = cloudinaryService.uploadFile(file);
                    listaImagenes.add(url);
                }
            }
        }
        
        if (productoRequest.getImagenes() != null) {
            listaImagenes.addAll(productoRequest.getImagenes());
        }

        if (listaImagenes.isEmpty()) {
            listaImagenes.add("https://res.cloudinary.com/dacnqinsu/image/upload/v1/default-product.png");
        }

        var entity = new ProductosEntity();
        BeanUtils.copyProperties(productoRequest, entity);
        
        entity.setImagenes(listaImagenes);
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
                case "precio_asc": sort = Sort.by("precio").ascending();
                break;
                case "precio_desc": sort = Sort.by("precio").descending(); break;
                case "nombre_asc": sort = Sort.by("nombre").ascending(); break;
                case "nombre_desc": sort = Sort.by("nombre").descending(); break;
            }
        }

        // Protección contra repositorio devolviendo null
        return Optional.ofNullable(productosRepository.findByTiendaNombreUrl(nombreTienda, sort))
                 .orElseGet(Collections::emptyList)
                 .stream()
                 .filter(p -> p != null) // Protección adicional por si la lista contiene nulls
                 .filter(ProductosEntity::getActivo) 
                 .map(this::convertirEntidadAResponse)
                 .collect(Collectors.toList());
    }

    @Override
    public List<ProductosResponse> buscarPorNombre(String nombreTienda, String termino) {
        // Protección contra repositorio devolviendo null
        return Optional.ofNullable(productosRepository.findByTiendaNombreUrlAndNombreContainingIgnoreCase(nombreTienda, termino))
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(ProductosEntity::getActivo) 
                .map(this::convertirEntidadAResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductosResponse> buscarPorCategoria(String nombreTienda, Long categoriaId) {
        // Protección contra repositorio devolviendo null
        return Optional.ofNullable(productosRepository.findByTiendaNombreUrlAndCategoriaId(nombreTienda, categoriaId))
                .orElseGet(Collections::emptyList)
                .stream()
                .filter(ProductosEntity::getActivo) 
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
    public ProductosResponse update(Long id, ProductosRequest productoRequest, List<MultipartFile> files) {
        final var entityFromDB = this.productosRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con id: " + id));
        validarDueño(entityFromDB.getTienda());

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
        
        if (productoRequest.getActivo() != null) {
            entityFromDB.setActivo(productoRequest.getActivo());
        }
        
        // --- INICIO MODIFICACIÓN UPDATE: ASIGNACIÓN SEGURA DE CATEGORÍA ---
        if (productoRequest.getCategoriaId() != null) {
            // Usamos la nueva lógica para asegurar la categoría o asignar "Otros"
            var categoria = asignarCategoriaSegura(productoRequest.getCategoriaId(), entityFromDB.getTienda());
            
            // La validación de pertenencia a la tienda ya está dentro de asignarCategoriaSegura,
            // pero la dejamos aquí para un manejo explícito si se necesitara, aunque es redundante.
            if (!categoria.getTienda().getId().equals(entityFromDB.getTienda().getId())) {
                throw new IllegalArgumentException("Error: No puedes mover este producto a una categoría de otra tienda.");
            }
            
            entityFromDB.setCategoria(categoria);
        }
        // --- FIN MODIFICACIÓN UPDATE ---
        
        List<String> imagenesFinales = (productoRequest.getImagenes() != null) 
                                       ? new ArrayList<>(productoRequest.getImagenes()) 
                                       : new ArrayList<>(entityFromDB.getImagenes());
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String url = cloudinaryService.uploadFile(file);
                    imagenesFinales.add(url);
                }
            }
        }
        
        entityFromDB.setImagenes(imagenesFinales);
        var productoActualizado = this.productosRepository.save(entityFromDB);
        return convertirEntidadAResponse(productoActualizado);
    }

    @Override
    public void delete(Long id) {
        var producto = this.productosRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con id: " + id));
        validarDueño(producto.getTienda());
        
        if (producto.getActivo() == null || producto.getActivo()) {
            producto.setActivo(false);
            this.productosRepository.save(producto);
        }
    }

    private void validarDueño(TiendaEntity tienda) {
        UsuariosEntity usuarioLogueado = (UsuariosEntity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!tienda.getVendedor().getEmail().equals(usuarioLogueado.getEmail())) {
            throw new IllegalArgumentException("ACCESO DENEGADO: No eres el dueño de esta tienda (Email incorrecto).");
        }
        if (!tienda.getVendedor().getDni().equals(usuarioLogueado.getDni())) {
            throw new IllegalArgumentException("ACCESO DENEGADO: No eres el dueño de esta tienda (DNI incorrecto).");
        }
    }
    
    private CategoriasEntity asignarCategoriaSegura(Long categoriaId, TiendaEntity tienda) {
        var categoriaOptional = categoriasRepository.findById(categoriaId);

        if (categoriaOptional.isPresent()) {
            CategoriasEntity categoria = categoriaOptional.get();
            if (!categoria.getTienda().getId().equals(tienda.getId())) {
                throw new IllegalArgumentException("Error de Seguridad: La categoría no pertenece a esta tienda.");
            }
            return categoria;
        }

        CategoriasEntity categoriaOtros = categoriasRepository.findByTiendaNombreUrl(tienda.getNombreUrl()).stream()
            .filter(cat -> "Otros".equalsIgnoreCase(cat.getNombre()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Error de configuración: No se encontró la categoría 'Otros' para la tienda " + tienda.getNombreFantasia()));
        
        log.warn("El producto fue asignado automáticamente a la categoría 'Otros' porque el ID {} no era válido.", categoriaId);
        return categoriaOtros;
    }

    private ProductosResponse convertirEntidadAResponse(ProductosEntity entidad) {
        var response = new ProductosResponse();
        BeanUtils.copyProperties(entidad, response);

        if (entidad.getCategoria() != null) {
            response.setCategoriaId(entidad.getCategoria().getId());
            response.setCategoriaNombre(
                entidad.getCategoria().getNombre() != null 
                ? entidad.getCategoria().getNombre() 
                : "Otros" 
            );
        } else {
            response.setCategoriaId(null);
            response.setCategoriaNombre("Otros"); 
        }
        
        return response;
    }
}