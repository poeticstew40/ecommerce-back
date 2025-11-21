package back.ecommerce.controllers;

import back.ecommerce.entities.PedidosEntity;
import back.ecommerce.repositories.PedidosRepository;
import back.ecommerce.services.MercadoPagoService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/pagos")
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class MercadoPagoController {

    private final MercadoPagoService mercadoPagoService;
    private final PedidosRepository pedidosRepository;

    // Endpoint: POST /api/pagos/crear/{pedidoId}
    @PostMapping("/crear/{pedidoId}")
    public ResponseEntity<Map<String, String>> crearLinkDePago(@PathVariable Long pedidoId) {
        
        // 1. Buscar el pedido en tu base de datos
        PedidosEntity pedido = pedidosRepository.findById(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado"));

        // 2. Generar el link con MercadoPago
        String urlPago = mercadoPagoService.crearPreferencia(pedido);

        // 3. Devolver la URL al frontend
        return ResponseEntity.ok(Map.of("url", urlPago));
    }
}