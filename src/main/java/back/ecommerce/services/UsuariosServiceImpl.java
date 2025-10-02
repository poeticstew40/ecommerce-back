package back.ecommerce.services;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import back.ecommerce.dtos.UsuariosRequest;
import back.ecommerce.dtos.UsuariosResponse;
import back.ecommerce.entities.UsuariosEntity;
import back.ecommerce.repositories.UsuariosRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
@AllArgsConstructor
public class UsuariosServiceImpl implements UsuariosService {

    private final UsuariosRepository usuariosRepository;

    @Override
    public UsuariosResponse create(UsuariosRequest usuario) {
        final var entity = new UsuariosEntity(); //create object(entity) to persist in database
        BeanUtils.copyProperties(usuario, entity); // copy properties from argument(usuario) in entity
 
        this.usuariosRepository.save(entity);

        return new UsuariosResponse();
    }

    @Override
    public UsuariosResponse readById(Long dni) {
        final var entityResponse = this.usuariosRepository.findById(dni)
        .orElseThrow(() -> new IllegalArgumentException("No existe el usuario con id: " + dni)); // Find by id and handle errors
        
        final var response = new UsuariosResponse(); // create response object

        BeanUtils.copyProperties(entityResponse, response); // copy properties from entity to response

      return response;
    }

    @Override
    public UsuariosResponse update(Long id, UsuariosRequest usuario) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void delete(Long id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
