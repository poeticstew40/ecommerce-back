package back.ecommerce.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import back.ecommerce.entities.ProductosEntity;

public interface ProductosRepository extends JpaRepository<ProductosEntity, Long>{

    List<ProductosEntity> findByNombreContainingIgnoreCase(String termino);

    List<ProductosEntity> findByCategoriaId(Long categoriaId);

}
