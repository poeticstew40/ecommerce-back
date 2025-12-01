package back.ecommerce.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import back.ecommerce.entities.ItemsPedidosEntity;
import back.ecommerce.entities.PedidosEntity;
import back.ecommerce.repositories.PedidosRepository;
import back.ecommerce.repositories.ProductosRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderCleanupService {

    private final PedidosRepository pedidosRepository;
    private final ProductosRepository productosRepository;

    @Scheduled(fixedRate = 600000)
    @Transactional
    public void cancelarPedidosExpirados() {
        log.info("[CRON JOB] Iniciando limpieza de pedidos expirados...");
        
        LocalDateTime tiempoLimite = LocalDateTime.now().minusMinutes(20);
        List<PedidosEntity> pedidosViejos = pedidosRepository.findByEstadoAndFechaPedidoBefore("PENDIENTE", tiempoLimite);

        if (pedidosViejos.isEmpty()) {
            log.info("[CRON JOB] No se encontraron pedidos pendientes para cancelar.");
            return;
        }

        log.info("[CRON JOB] Se encontraron {} pedidos expirados. Procesando devoluciones...", pedidosViejos.size());

        for (PedidosEntity pedido : pedidosViejos) {
            try {
                log.info("Cancelando Pedido #{}...", pedido.getId());
                
                for (ItemsPedidosEntity item : pedido.getItemsPedido()) {
                    var producto = item.getProducto();
                    int cantidadARestaurar = item.getCantidad();
                    
                    producto.setStock(producto.getStock() + cantidadARestaurar);
                    productosRepository.save(producto);
                    
                    log.info("-> Producto '{}': Stock restaurado +{}", producto.getNombre(), cantidadARestaurar);
                }

                pedido.setEstado("CANCELADO");
                pedidosRepository.save(pedido);
                
            } catch (Exception e) {
                log.error("Error procesando limpieza del pedido #{}: {}", pedido.getId(), e.getMessage());
            }
        }
        log.info("[CRON JOB] Limpieza finalizada exitosamente.");
    }
}