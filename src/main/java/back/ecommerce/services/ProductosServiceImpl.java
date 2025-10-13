package back.ecommerce.services;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import back.ecommerce.dtos.ProductosRequest;
import back.ecommerce.dtos.ProductosResponse;
import back.ecommerce.entities.ProductosEntity;
import back.ecommerce.repositories.ProductosRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
@AllArgsConstructor
public class ProductosServiceImpl implements ProductosService{

    private final ProductosRepository productosRepository;

    @Override
    public ProductosResponse create(ProductosRequest producto) {
        var entity = new ProductosEntity();
        BeanUtils.copyProperties(producto, entity);

        var productoCreated = productosRepository.save(entity);

        var response = new ProductosResponse();
        BeanUtils.copyProperties(productoCreated, response);


        return response;
    }

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
    public ProductosResponse update(Long id, ProductosRequest producto) {
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public void delete(Long id) {
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }


}
