package back.ecommerce.services;

import back.ecommerce.dtos.ProductosRequest;
import back.ecommerce.dtos.ProductosResponse;

public interface ProductosService {
    //Crear
    ProductosResponse create(ProductosResponse producto); 
    //Obtener por id
    ProductosResponse readById(Long id);
    //Actualizar
    ProductosResponse update(Long id, ProductosRequest producto);
    //Eliminar
    void delete(Long id);
    
}
