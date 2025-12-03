package back.ecommerce.services;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import back.ecommerce.dtos.ProductosRequest;
import back.ecommerce.dtos.ProductosResponse;

public interface ProductosService {
    ProductosResponse create(String nombreTienda, ProductosRequest producto, List<MultipartFile> files);
    List<ProductosResponse> readAllByTienda(String nombreTienda, String orden);
    List<ProductosResponse> buscarPorNombre(String nombreTienda, String termino);
    List<ProductosResponse> buscarPorCategoria(String nombreTienda, Long categoriaId);
    ProductosResponse readById(Long id);
    ProductosResponse update(Long id, ProductosRequest producto, List<MultipartFile> files);
    void delete(Long id);
}