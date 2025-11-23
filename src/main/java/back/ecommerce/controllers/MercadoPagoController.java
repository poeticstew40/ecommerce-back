package back.ecommerce.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import back.ecommerce.entities.PedidosEntity;
import back.ecommerce.repositories.PedidosRepository;
import back.ecommerce.services.MercadoPagoService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/pagos")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class MercadoPagoController {

    private final MercadoPagoService mercadoPagoService;
    private final PedidosRepository pedidosRepository;

    @PostMapping("/crear/{pedidoId}")
    public ResponseEntity<?> crearLinkDePago(@PathVariable Long pedidoId) {
        
        PedidosEntity pedido = pedidosRepository.findById(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        if ("PAGADO".equalsIgnoreCase(pedido.getEstado()) || "APROBADO".equalsIgnoreCase(pedido.getEstado())) {
            return ResponseEntity.badRequest().body("Este pedido ya fue pagado.");
        }

        // Delegamos al servicio que ahora lee las credenciales de la configuración
        String urlPago = mercadoPagoService.crearPreferencia(pedido);
        
        return ResponseEntity.ok(Map.of("url", urlPago));
    }
    
    @PostMapping("/webhook")
    public ResponseEntity<Void> recibirNotificacion(
            @RequestParam(value = "topic", required = false) String topic,
            @RequestParam(value = "id", required = false) Long id) {

        if ("payment".equals(topic) && id != null) {
            System.out.println("🔔 Notificación de Pago recibida. ID: " + id);
            mercadoPagoService.procesarNotificacion(id);
        }
        
        return ResponseEntity.ok().build();
    }
}