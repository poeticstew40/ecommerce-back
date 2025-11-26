package back.ecommerce.services;

import java.util.List;
import back.ecommerce.dtos.CategoriasRequest;
import back.ecommerce.dtos.CategoriasResponse;

public interface CategoriasService {
    CategoriasResponse create(String nombreTienda, CategoriasRequest request);
    List<CategoriasResponse> readAllByTienda(String nombreTienda);
    CategoriasResponse readById(Long id);
    CategoriasResponse update(Long id, CategoriasRequest request);
    void delete(Long id);
}