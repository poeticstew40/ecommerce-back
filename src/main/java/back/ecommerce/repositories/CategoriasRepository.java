package back.ecommerce.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import back.ecommerce.entities.CategoriasEntity;

public interface CategoriasRepository extends JpaRepository<CategoriasEntity, Long>{



    // Traer todas las categorías DE ESA TIENDA
    List<CategoriasEntity> findByTiendaNombreUrl(String nombreUrl);

}
