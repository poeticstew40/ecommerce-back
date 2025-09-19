package back.ecommerce.services;

import back.ecommerce.dtos.CategoriasRequest;
import back.ecommerce.dtos.CategoriasResponse;
import back.ecommerce.repositories.CategoriasRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
@AllArgsConstructor
public class CategoriasServiceImpl implements CategoriasService{

    private final CategoriasRepository categoriasRepository;

    @Override
    public CategoriasResponse create(CategoriasRequest categoria) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CategoriasResponse readById(Long id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CategoriasResponse update(Long id, CategoriasRequest categoria) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void delete(Long id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
