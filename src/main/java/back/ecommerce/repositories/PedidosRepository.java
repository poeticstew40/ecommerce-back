package back.ecommerce.repositories;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import back.ecommerce.entities.PedidosEntity;

public interface PedidosRepository extends JpaRepository<PedidosEntity, Long>{
    List<PedidosEntity> findByTiendaNombreUrl(String nombreUrl);
    List<PedidosEntity> findByTiendaNombreUrlAndUsuarioDni(String nombreUrl, Long dni);
    List<PedidosEntity> findByEstadoAndFechaPedidoBefore(String estado, LocalDateTime fechaLimite);
    List<PedidosEntity> findByUsuarioDni(Long dni);
}