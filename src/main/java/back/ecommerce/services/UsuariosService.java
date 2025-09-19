package back.ecommerce.services;

import back.ecommerce.dtos.UsuariosRequest;
import back.ecommerce.dtos.UsuariosResponse;

public interface UsuariosService {

    //Crear
    UsuariosResponse create(UsuariosRequest usuario);
    //Obtener por id
    UsuariosResponse readById(Long id);
    //Actualizar
    UsuariosResponse update(Long id, UsuariosRequest usuario);
    //Eliminar
    void delete(Long id);

}
