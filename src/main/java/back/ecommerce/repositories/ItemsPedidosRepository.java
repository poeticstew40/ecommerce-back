package back.ecommerce.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import back.ecommerce.entities.ItemsPedidosEntity;

public interface ItemsPedidosRepository extends JpaRepository<ItemsPedidosEntity, Long>{

}