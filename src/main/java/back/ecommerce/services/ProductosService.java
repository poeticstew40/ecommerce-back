package back.ecommerce.services;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import back.ecommerce.dtos.ProductosRequest;
import back.ecommerce.dtos.ProductosResponse;

public interface ProductosService {
    ProductosResponse create(String nombreTienda, ProductosRequest producto);
    ProductosResponse create(String nombreTienda, ProductosRequest producto, MultipartFile file);
    List<ProductosResponse> readAllByTienda(String nombreTienda);
    List<ProductosResponse> buscarPorNombre(String nombreTienda, String termino);
    List<ProductosResponse> buscarPorCategoria(String nombreTienda, Long categoriaId);
    ProductosResponse readById(Long id);
    ProductosResponse update(Long id, ProductosRequest producto);
    void delete(Long id);
}