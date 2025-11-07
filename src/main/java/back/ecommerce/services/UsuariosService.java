package back.ecommerce.services;

import java.util.List;

import back.ecommerce.dtos.UsuariosRequest;
import back.ecommerce.dtos.UsuariosResponse;

public interface UsuariosService {

    //Crear
    UsuariosResponse create(UsuariosRequest usuario);
    
    //Obtener todos
    List<UsuariosResponse> readAll();

    //Obtener por id
    UsuariosResponse readByDni(Long dni);

    //Actualizar
    UsuariosResponse update(Long dni, UsuariosRequest usuario);
    
    //Eliminar
    void delete(Long dni);

}
