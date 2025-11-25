package back.ecommerce.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import back.ecommerce.entities.ItemCarritoEntity;

public interface CarritoRepository extends JpaRepository<ItemCarritoEntity, Long> {

    // Buscar todo el carrito de un usuario
    List<ItemCarritoEntity> findByUsuarioDni(Long dni);

    // Buscar si ya existe este producto en el carrito de este usuario (para sumar cantidad y no duplicar)
    Optional<ItemCarritoEntity> findByUsuarioDniAndProductoId(Long dni, Long productoId);

    // Vaciar carrito del usuario
    void deleteByUsuarioDni(Long dni);
}