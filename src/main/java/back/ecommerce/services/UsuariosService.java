package back.ecommerce.services;

import back.ecommerce.dtos.UsuariosRequest;
import back.ecommerce.dtos.UsuariosResponse;

public interface UsuariosService {

    //Crear
    UsuariosResponse create(UsuariosRequest usuario);

    //Obtener por id
    UsuariosResponse readById(Long dni);

    //Actualizar
    UsuariosResponse update(Long dni, UsuariosRequest usuario);
    
    //Eliminar
    void delete(Long dni);

}
