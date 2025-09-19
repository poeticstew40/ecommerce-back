package back.ecommerce.services;

import back.ecommerce.dtos.CategoriasRequest;
import back.ecommerce.dtos.CategoriasResponse;

public interface CategoriasService {

    //Crear
    CategoriasResponse create(CategoriasRequest categoria);
    //Obtener por id
    CategoriasResponse readById(Long id);
    //Actualizar
    CategoriasResponse update(Long id, CategoriasRequest categoria);
    //Eliminar
    void delete(Long id);
    

}
