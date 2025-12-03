package back.ecommerce.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import back.ecommerce.entities.TiendaEntity;

public interface TiendaRepository extends JpaRepository<TiendaEntity, Long> {
    Optional<TiendaEntity> findByNombreUrl(String nombreUrl);
    Optional<TiendaEntity> findByVendedorDni(Long vendedorDni);
}