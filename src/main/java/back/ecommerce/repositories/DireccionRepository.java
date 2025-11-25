package back.ecommerce.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import back.ecommerce.entities.DireccionEntity;

public interface DireccionRepository extends JpaRepository<DireccionEntity, Long> {
    
    // Buscar todas las direcciones de un usuario
    List<DireccionEntity> findByUsuarioDni(Long dni);
}