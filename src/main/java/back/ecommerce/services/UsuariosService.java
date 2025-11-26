package back.ecommerce.services;

import java.util.List;
import back.ecommerce.dtos.UsuariosRequest;
import back.ecommerce.dtos.UsuariosResponse;

public interface UsuariosService {
    List<UsuariosResponse> readAll();
    UsuariosResponse readByDni(Long dni);
    UsuariosResponse update(Long dni, UsuariosRequest usuario);
    void delete(Long dni);
}