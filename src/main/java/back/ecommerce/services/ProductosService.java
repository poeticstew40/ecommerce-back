package back.ecommerce.services;

import back.ecommerce.dtos.ProductosRequest;
import back.ecommerce.dtos.ProductosResponse;

public interface ProductosService {
    //Crear
    ProductosResponse create(ProductosRequest producto); 
    //Obtener por id
    ProductosResponse readById(Long id);
    //Obtener por nombre
    ProductosResponse readByName(String nombre);
    //Actualizar
    ProductosResponse update(Long id, ProductosRequest producto);
    //Eliminar
    void delete(Long id);
    
}
