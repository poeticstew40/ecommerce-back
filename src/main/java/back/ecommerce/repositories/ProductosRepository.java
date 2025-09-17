package back.ecommerce.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import back.ecommerce.entities.ProductosEntity;

public interface ProductosRepository extends JpaRepository<ProductosEntity, Long>{

}
