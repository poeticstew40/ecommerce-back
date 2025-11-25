package back.ecommerce.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import back.ecommerce.entities.ProductosEntity;

public interface ProductosRepository extends JpaRepository<ProductosEntity, Long>{

    // Buscar productos de una tienda específica (usando el slug de la URL)
    List<ProductosEntity> findByTiendaNombreUrl(String nombreUrl);

    // Buscar por nombre, PERO solo dentro de esa tienda (para que no busque en otras)
    List<ProductosEntity> findByTiendaNombreUrlAndNombreContainingIgnoreCase(String nombreUrl, String termino);

    // Filtrar por categoría, PERO solo dentro de esa tienda
    List<ProductosEntity> findByTiendaNombreUrlAndCategoriaId(String nombreUrl, Long categoriaId);

}
