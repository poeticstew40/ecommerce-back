package back.ecommerce.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import back.ecommerce.entities.FavoritoEntity;

public interface FavoritoRepository extends JpaRepository<FavoritoEntity, Long> {
    List<FavoritoEntity> findByUsuarioDniAndProducto_Tienda_NombreUrl(Long dni, String nombreUrl);
    boolean existsByUsuarioDniAndProductoId(Long dni, Long productoId);
    void deleteByUsuarioDniAndProductoId(Long dni, Long productoId);
}