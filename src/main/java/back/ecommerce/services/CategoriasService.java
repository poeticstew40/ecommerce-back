package back.ecommerce.services;

import java.util.List;

import back.ecommerce.dtos.CategoriasRequest;
import back.ecommerce.dtos.CategoriasResponse;

public interface CategoriasService {

    // Crear una categoría asignada a una tienda específica
    CategoriasResponse create(String nombreTienda, CategoriasRequest request);

    // Leer todas las categorías de una tienda específica
    List<CategoriasResponse> readAllByTienda(String nombreTienda);

    // Métodos por ID (Siguen igual porque el ID es único)
    CategoriasResponse readById(Long id);
    CategoriasResponse update(Long id, CategoriasRequest request);
    void delete(Long id);
}