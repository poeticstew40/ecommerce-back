package back.ecommerce.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import back.ecommerce.entities.ItemCarritoEntity;

public interface CarritoRepository extends JpaRepository<ItemCarritoEntity, Long> {
    List<ItemCarritoEntity> findByUsuarioDni(Long dni);
    Optional<ItemCarritoEntity> findByUsuarioDniAndProductoId(Long dni, Long productoId);
    void deleteByUsuarioDni(Long dni);
}