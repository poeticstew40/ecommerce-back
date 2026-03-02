package back.ecommerce.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import back.ecommerce.entities.TiendaEntity;

public interface TiendaRepository extends JpaRepository<TiendaEntity, Long> {
    Optional<TiendaEntity> findByNombreUrl(String nombreUrl);
    Optional<TiendaEntity> findByVendedorDni(Long vendedorDni);

    @Query("SELECT DISTINCT t FROM TiendaEntity t LEFT JOIN FETCH t.vendedor LEFT JOIN FETCH t.banners")
    List<TiendaEntity> findAllOptimizadas();
}