package back.ecommerce.services;

import back.ecommerce.dtos.UsuariosRequest;
import back.ecommerce.dtos.UsuariosResponse;
import back.ecommerce.repositories.UsuariosRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
        throw new UnsupportedOperationException("Unimplemented method 'create'");
    }

    @Override
    public UsuariosResponse readById(Long id) {
        throw new UnsupportedOperationException("Unimplemented method 'readById'");
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
