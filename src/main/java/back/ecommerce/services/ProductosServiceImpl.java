package back.ecommerce.services;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import back.ecommerce.dtos.ProductosRequest;
import back.ecommerce.dtos.ProductosResponse;
import back.ecommerce.entities.ProductosEntity;
import back.ecommerce.repositories.CategoriasRepository;
import back.ecommerce.repositories.ProductosRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
@AllArgsConstructor
public class ProductosServiceImpl implements ProductosService{

    private final ProductosRepository productosRepository;
    private final CategoriasRepository categoriasRepository;


    //create producto
    @Override
    public ProductosResponse create(ProductosRequest producto) {
        var entity = new ProductosEntity();
        BeanUtils.copyProperties(producto, entity);

        var categoria = categoriasRepository.findById(producto.getCategoriaId())
            .orElseThrow(() -> new IllegalArgumentException("Categoria no encontrada con id: " + producto.getCategoriaId()));

        entity.setCategoria(categoria);

        var productoCreated = productosRepository.save(entity);

        var response = new ProductosResponse();
        BeanUtils.copyProperties(productoCreated, response);
        response.setCategoriaNombre(productoCreated.getCategoria().getNombre());
        response.setCategoriaId(productoCreated.getCategoria().getId());


        return response;
    }

    //get producto by id
    @Override
    public ProductosResponse readById(Long id) {
        final var entityResponse = this.productosRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con id: " + id));

        var response = new ProductosResponse();
        BeanUtils.copyProperties(entityResponse, response);

        if (entityResponse.getCategoria() != null) {
            response.setCategoriaId(entityResponse.getCategoria().getId());
            response.setCategoriaNombre(entityResponse.getCategoria().getNombre());
        }
        
        return response;
    }

    
    @Override
    public ProductosResponse readByName(String nombre) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

@Override
public ProductosResponse update(Long id, ProductosRequest productoRequest) {
    // 1. Busca el producto en la base de datos o lanza una excepción.
    final var entityFromDB = this.productosRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con id: " + id));

    // 2. Aplica las actualizaciones campo por campo, solo si no son nulos.
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

    // 3. Mantiene la lógica para actualizar la categoría si se provee una nueva.
    if (productoRequest.getCategoriaId() != null) {
        var categoria = categoriasRepository.findById(productoRequest.getCategoriaId())
            .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con id: " + productoRequest.getCategoriaId()));
        entityFromDB.setCategoria(categoria);
    }
    
    // 4. Guarda la entidad con los cambios aplicados.
    var productoActualizado = this.productosRepository.save(entityFromDB);

    // 5. Crea y rellena el DTO de respuesta.
    final var response = new ProductosResponse();
    BeanUtils.copyProperties(productoActualizado, response);

    if (productoActualizado.getCategoria() != null) {
        response.setCategoriaId(productoActualizado.getCategoria().getId());
        response.setCategoriaNombre(productoActualizado.getCategoria().getNombre());
    }

    return response;
}

    @Override
    public void delete(Long id) {
        var producto = this.productosRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con id: " + id));

        log.info("Eliminando producto: {}", producto.getNombre());

        this.productosRepository.delete(producto);
    }


}
