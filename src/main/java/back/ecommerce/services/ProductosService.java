package back.ecommerce.services;

import java.util.List;

import back.ecommerce.dtos.ProductosRequest;
import back.ecommerce.dtos.ProductosResponse;

public interface ProductosService {
    
    // Crear asignando a una tienda
    ProductosResponse create(String nombreTienda, ProductosRequest producto);

    // Leer solo los de esa tienda
    List<ProductosResponse> readAllByTienda(String nombreTienda);

    // Búsquedas filtradas por tienda
    List<ProductosResponse> buscarPorNombre(String nombreTienda, String termino);
    List<ProductosResponse> buscarPorCategoria(String nombreTienda, Long categoriaId);

    // Métodos por ID (Siguen igual porque el ID es único global)
    ProductosResponse readById(Long id);
    ProductosResponse update(Long id, ProductosRequest producto);
    void delete(Long id);
    
}
