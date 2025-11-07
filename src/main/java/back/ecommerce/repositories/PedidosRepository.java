package back.ecommerce.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import back.ecommerce.entities.PedidosEntity;

public interface PedidosRepository extends JpaRepository<PedidosEntity, Long>{

    //query
    Optional<PedidosEntity> findById(Long id);

    //delete
    @Modifying
    @Query("DELETE FROM PedidosEntity WHERE id=:id")
    void deleteById(Long id);

    List<PedidosEntity> findByUsuarioDni(Long dni);

}
