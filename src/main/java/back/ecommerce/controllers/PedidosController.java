package back.ecommerce.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import back.ecommerce.dtos.PedidosRequest;
import back.ecommerce.dtos.PedidosResponse;
import back.ecommerce.services.PedidosService;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/tienda/{nombreTienda}/pedidos") // 👈 Ruta base dinámica
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class PedidosController {

    private final PedidosService pedidosService;

    // 1. Crear Pedido en la tienda
    @PostMapping
    public ResponseEntity<PedidosResponse> postPedidos(
            @PathVariable String nombreTienda,
            @RequestBody PedidosRequest request) {

        final var pedido = this.pedidosService.create(nombreTienda, request);
        
        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/tienda/{nombreTienda}/pedidos/{id}")
                .buildAndExpand(nombreTienda, pedido.getId())
                .toUri();
                
        return ResponseEntity.created(location).body(pedido);
    }

    // 2. Ver todos los pedidos de la tienda (Para el admin/vendedor)
    @GetMapping
    public ResponseEntity<List<PedidosResponse>> getAllByTienda(@PathVariable String nombreTienda) {
        return ResponseEntity.ok(this.pedidosService.readAllByTienda(nombreTienda));
    }

    // 3. Ver pedidos de un usuario específico en esta tienda
    @GetMapping("/usuario/{dni}")
    public ResponseEntity<List<PedidosResponse>> getPedidosByUsuarioDni(
            @PathVariable String nombreTienda,
            @PathVariable Long dni) {
        return ResponseEntity.ok(this.pedidosService.findByUsuarioDni(nombreTienda, dni));
    }

    // --- Métodos por ID ---

    @GetMapping("/{id}")
    public ResponseEntity<PedidosResponse> getPedidosById(
            @PathVariable String nombreTienda,
            @PathVariable Long id) {
        return ResponseEntity.ok(this.pedidosService.readById(id));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PedidosResponse> updatePedidos(
            @PathVariable String nombreTienda,
            @PathVariable Long id, 
            @RequestBody PedidosRequest request) {
        return ResponseEntity.ok(this.pedidosService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePedidos(
            @PathVariable String nombreTienda,
            @PathVariable Long id) {
        this.pedidosService.delete(id);
        return ResponseEntity.noContent().build();
    }
}