package back.ecommerce.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import back.ecommerce.entities.PedidosEntity;

public interface PedidosRepository extends JpaRepository<PedidosEntity, Long>{

    // Ver todos los pedidos recibidos por UNA TIENDA (para el panel del vendedor)
    List<PedidosEntity> findByTiendaNombreUrl(String nombreUrl);

    // Ver los pedidos que hizo UN COMPRADOR en una tienda específica
    List<PedidosEntity> findByTiendaNombreUrlAndUsuarioDni(String nombreUrl, Long dni);

    // Buscar pedidos por estado y fecha límite
    List<PedidosEntity> findByEstadoAndFechaPedidoBefore(String estado, LocalDateTime fechaLimite);

}
