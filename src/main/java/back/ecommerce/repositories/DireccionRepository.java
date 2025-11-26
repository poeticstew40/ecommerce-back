package back.ecommerce.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import back.ecommerce.entities.DireccionEntity;

public interface DireccionRepository extends JpaRepository<DireccionEntity, Long> {
    List<DireccionEntity> findByUsuarioDni(Long dni);
}