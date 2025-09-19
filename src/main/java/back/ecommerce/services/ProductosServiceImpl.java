package back.ecommerce.services;

import back.ecommerce.dtos.ProductosRequest;
import back.ecommerce.dtos.ProductosResponse;
import back.ecommerce.repositories.ProductosRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
@AllArgsConstructor
public class ProductosServiceImpl implements ProductosService{

    private final ProductosRepository productosRepository;

    @Override
    public ProductosResponse create(ProductosResponse producto) {
        throw new UnsupportedOperationException("Unimplemented method 'create'");
    }

    @Override
    public ProductosResponse readById(Long id) {
        throw new UnsupportedOperationException("Unimplemented method 'readById'");
    }

    @Override
    public ProductosResponse update(Long id, ProductosRequest producto) {
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public void delete(Long id) {
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

}
