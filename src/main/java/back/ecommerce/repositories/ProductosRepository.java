package back.ecommerce.repositories;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import back.ecommerce.entities.ProductosEntity;

public interface ProductosRepository extends JpaRepository<ProductosEntity, Long>{
    List<ProductosEntity> findByTiendaNombreUrl(String nombreUrl);
    List<ProductosEntity> findByTiendaNombreUrlAndNombreContainingIgnoreCase(String nombreUrl, String termino);
    List<ProductosEntity> findByTiendaNombreUrlAndCategoriaId(String nombreUrl, Long categoriaId);
    List<ProductosEntity> findByTiendaNombreUrl(String nombreUrl, Sort sort);
}